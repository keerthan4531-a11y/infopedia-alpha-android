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

        var contextText: String? = null

        // If Wikipedia context is available, build token-optimized RAG context
        if (wikipediaContext != null && (wikipediaContext.articles.isNotEmpty() || wikipediaContext.rankedChunks.isNotEmpty() || wikipediaContext.wikidataFacts.isNotEmpty())) {
            contextText = buildString {
                append("You are Infopedia Alpha's token-optimized RAG AI. Ground your answers strictly in the verified sources below.\n\n")

                // PRIORITY 1: Wikidata Structured Facts (Compact & High Accuracy)
                if (wikipediaContext.wikidataFacts.isNotEmpty()) {
                    append("=== WIKIDATA STRUCTURED FACTS (HIGH PRIORITY) ===\n")
                    wikipediaContext.wikidataFacts.forEach { fact ->
                        append("• ${fact.propertyName}: ${fact.value}\n")
                    }
                    append("\n")
                }

                // PRIORITY 2: Verified Article Summaries & Detailed Context
                if (wikipediaContext.articles.isNotEmpty()) {
                    append("=== VERIFIED ARTICLE SUMMARIES ===\n")
                    wikipediaContext.articles.forEachIndexed { index, article ->
                        append("Source [${index + 1}]: ${article.displayTitle} (${article.langCode.uppercase()}) > ${article.extract}\n\n")
                    }
                }

                // PRIORITY 3: Deep Section Facts
                if (wikipediaContext.rankedChunks.isNotEmpty()) {
                    append("=== DETAILED SECTION FACTS ===\n")
                    wikipediaContext.rankedChunks.forEachIndexed { index, chunk ->
                        val compressed = RagChunkSynthesizer.compressChunk(chunk)
                        append("Source Chunk [${index + 1}]: $compressed\n\n")
                    }
                }

                append("CRITICAL RAG RESPONSE RULES FOR HIGH-QUALITY DETAILED OUTPUT:\n")
                append("1. COMPREHENSIVE HIGH-DETAIL RESPONSE: Provide an expanded, rich, and deeply informative response synthesizing all facts from the verified sources above. Never output a brief 1-2 sentence response.\n")
                append("2. STRUCTURED FORMATTING (Skeleton-of-Thought): Organize your answer into clear Markdown sections:\n")
                append("   - Use bold headers (###) for distinct subtopics or historical/scientific periods.\n")
                append("   - Use bullet points (•) for key events, dates, milestones, and facts.\n")
                append("   - Bold key terms, names, dates, and concepts for readability.\n")
                append("3. INLINE CITATIONS: Insert citation tags [1], [2], [3] immediately after stating specific facts.\n")
                append("4. TAMIL LANGUAGE SUPPORT: If the user query is in Tamil or Tamil script, provide the entire explanation in fluent, eloquent Tamil.\n")
                append("5. FOLLOW-UP QUESTIONS: End your response with '### Related Questions' listing 3 engaging follow-up bullet points (- ...).\n")
            }

            // Standard system role message for models that support it
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", contextText)
            })
        }

        // Add chat messages with full multi-turn conversation context
        val validMessages = messages.filter { it.content.isNotBlank() }.takeLast(20)
        for (i in validMessages.indices) {
            val msg = validMessages[i]
            val isLastUserMsg = (i == validMessages.lastIndex && msg.role == AiChatMessage.ROLE_USER)
            
            // For workers/proxies (Qwen, Baidu, Grok, etc.) that strip 'system' roles,
            // inject RAG context into the final user prompt to guarantee 100% citation & follow-up enforcement
            val finalContent = if (isLastUserMsg && contextText != null) {
                "[WIKIDATA & WIKIPEDIA CONTEXT INJECTED]\n$contextText\n\n[USER QUESTION]\n${msg.content}"
            } else {
                msg.content
            }

            messagesArray.put(JSONObject().apply {
                put("role", msg.role)
                put("content", finalContent)
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
