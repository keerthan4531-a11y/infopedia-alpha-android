package org.wikipedia.ai

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.wikipedia.dataclient.okhttp.OkHttpConnectionFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object AiApiClient {

    private val streamingClient: OkHttpClient by lazy {
        OkHttpConnectionFactory.client.newBuilder()
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun streamChat(
        model: AiModel,
        messages: List<AiChatMessage>,
        wikipediaContext: WikipediaContext? = null
    ): Flow<StreamEvent> = callbackFlow {
        val messagesArray = JSONArray()

        // If Wikipedia context is available, add it as a full-power RAG system message
        if (wikipediaContext != null && (wikipediaContext.articles.isNotEmpty() || wikipediaContext.rankedChunks.isNotEmpty())) {
            val contextText = buildString {
                append("You are Infopedia Alpha's advanced RAG research AI with real-time multi-language Wikipedia integration.\n\n")

                if (wikipediaContext.wikidataFacts.isNotEmpty()) {
                    append("=== STRUCTURED WIKIDATA FACTS ===\n")
                    wikipediaContext.wikidataFacts.forEach { fact ->
                        append("• ${fact.propertyName}: ${fact.value}\n")
                    }
                    append("\n")
                }

                if (wikipediaContext.rankedChunks.isNotEmpty()) {
                    append("=== RANKED DEEP SECTION CHUNKS ===\n")
                    wikipediaContext.rankedChunks.forEachIndexed { index, chunk ->
                        append("Source [${index + 1}]: ${chunk.articleTitle} (${chunk.langCode.uppercase()}) -> ${chunk.sectionTitle}\n")
                        append("${chunk.content}\n\n")
                    }
                } else {
                    append("=== VERIFIED WIKIPEDIA SOURCES ===\n")
                    wikipediaContext.articles.forEachIndexed { index, article ->
                        append("Source [${index + 1}]: ${article.displayTitle} (${article.langCode.uppercase()})\n${article.extract}\n\n")
                    }
                }

                append("CRITICAL RAG RESPONSE RULES:\n")
                append("1. Answer thoroughly, accurately, and fluently based on the provided sources above.\n")
                append("2. Include inline citation tags [1], [2], [3] immediately following claims derived from those sources.\n")
                append("3. If query language is Tamil or Malayalam, respond fluently in that language using cross-language context.\n")
                append("4. Conclude your response with a '### Related Questions' section listing 3 short follow-up questions formatted as bullet points (- ...).\n")
            }
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", contextText)
            })
        }

        // Add chat messages with full multi-turn conversation context (ChatGPT style)
        val validMessages = messages.filter { it.content.isNotBlank() }.takeLast(20)
        for (msg in validMessages) {
            messagesArray.put(JSONObject().apply {
                put("role", msg.role)
                put("content", msg.content)
            })
        }

        val requestBody = JSONObject().apply {
            put("model", model.modelStr)
            put("messages", messagesArray)
            put("stream", true)
            if (model.enableDeepThink) {
                put("enable_deep_think", true)
            }
        }

        val request = Request.Builder()
            .url(model.baseUrl)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json, text/event-stream, */*")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val call = streamingClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) {
                    trySend(StreamEvent.Error(e.message ?: "Network connection error"))
                }
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    trySend(StreamEvent.Error("HTTP ${response.code}: ${response.message}"))
                    response.close()
                    close()
                    return
                }

                val body = response.body
                if (body == null) {
                    trySend(StreamEvent.Error("Empty response body from AI worker"))
                    response.close()
                    close()
                    return
                }

                try {
                    val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                    var line: String?
                    var hasEmittedToken = false

                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: continue
                        if (currentLine.isEmpty()) continue

                        val payload = when {
                            currentLine.startsWith("data:") -> currentLine.substring(5).trim()
                            currentLine.startsWith("data ") -> currentLine.substring(5).trim()
                            currentLine.startsWith("{") -> currentLine
                            else -> null
                        }

                        if (payload == "[DONE]" || currentLine.contains("[DONE]")) {
                            trySend(StreamEvent.Done)
                            break
                        }

                        if (payload != null) {
                            try {
                                val json = JSONObject(payload)
                                val choices = json.optJSONArray("choices")

                                if (choices != null && choices.length() > 0) {
                                    val choice = choices.getJSONObject(0)
                                    val delta = choice.optJSONObject("delta")
                                    val message = choice.optJSONObject("message")

                                    // Extract DeepThink / reasoning tokens
                                    val thinking = delta?.optString("reasoning_content", "")?.ifEmpty {
                                        delta?.optString("thinking", "")?.ifEmpty {
                                            delta?.optString("reasoning", "")?.ifEmpty {
                                                message?.optString("reasoning_content", "")?.ifEmpty {
                                                    json.optString("reasoning_content", "")
                                                }
                                            }
                                        }
                                    } ?: ""

                                    if (thinking.isNotEmpty()) {
                                        trySend(StreamEvent.ThinkingToken(thinking))
                                        hasEmittedToken = true
                                    }

                                    // Extract content tokens
                                    val content = delta?.optString("content", "")?.ifEmpty {
                                        message?.optString("content", "")?.ifEmpty {
                                            choice.optString("text", "")
                                        }
                                    } ?: ""

                                    if (content.isNotEmpty()) {
                                        trySend(StreamEvent.Token(content))
                                        hasEmittedToken = true
                                    }

                                    val finishReason = choice.optString("finish_reason", "")
                                    if (finishReason == "stop") {
                                        trySend(StreamEvent.Done)
                                    }
                                } else {
                                    // Direct JSON response format fallback
                                    val directContent = json.optString("content", "")
                                        .ifEmpty { json.optString("text", "") }
                                        .ifEmpty { json.optString("response", "") }

                                    if (directContent.isNotEmpty()) {
                                        trySend(StreamEvent.Token(directContent))
                                        hasEmittedToken = true
                                    }
                                }
                            } catch (_: Exception) {
                                // Raw text line fallback if JSON parse fails
                                if (!currentLine.startsWith("<")) {
                                    trySend(StreamEvent.Token(currentLine + "\n"))
                                    hasEmittedToken = true
                                }
                            }
                        } else {
                            // Plain text response line fallback
                            if (!currentLine.startsWith("<")) {
                                trySend(StreamEvent.Token(currentLine + "\n"))
                                hasEmittedToken = true
                            }
                        }
                    }

                    if (hasEmittedToken) {
                        trySend(StreamEvent.Done)
                    } else {
                        trySend(StreamEvent.Error("No response token received from worker."))
                    }

                    reader.close()
                } catch (e: Exception) {
                    if (!call.isCanceled()) {
                        trySend(StreamEvent.Error(e.message ?: "Stream reading error"))
                    }
                } finally {
                    response.close()
                    close()
                }
            }
        })

        awaitClose {
            try {
                call.cancel()
            } catch (_: Exception) {}
        }
    }

    sealed class StreamEvent {
        data class Token(val text: String) : StreamEvent()
        data class ThinkingToken(val text: String) : StreamEvent()
        data class Error(val message: String) : StreamEvent()
        data object Done : StreamEvent()
    }
}
