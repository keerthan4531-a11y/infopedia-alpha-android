package org.wikipedia.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InixaAlphaUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val selectedModel: AiModel = AiModel.DEFAULT_MODEL,
    val isWikipediaConnected: Boolean = false,
    val isStreaming: Boolean = false,
    val wikipediaStatus: WikipediaSearchStatus? = null,
    val showModelSelector: Boolean = false,
    val currentThinkingContent: String = "",
    val errorMessage: String? = null
)

class InixaAlphaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InixaAlphaUiState())
    val uiState: StateFlow<InixaAlphaUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    fun selectModel(model: AiModel) {
        _uiState.update { it.copy(selectedModel = model, showModelSelector = false) }
    }

    fun toggleWikipedia() {
        _uiState.update { it.copy(isWikipediaConnected = !it.isWikipediaConnected) }
    }

    fun toggleModelSelector() {
        _uiState.update { it.copy(showModelSelector = !it.showModelSelector) }
    }

    fun dismissModelSelector() {
        _uiState.update { it.copy(showModelSelector = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // If previously streaming, stop it cleanly
        if (_uiState.value.isStreaming) {
            stopStreaming()
        }

        val userMessage = AiChatMessage(
            role = AiChatMessage.ROLE_USER,
            content = text
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isStreaming = true,
                errorMessage = null,
                currentThinkingContent = ""
            )
        }

        streamingJob = viewModelScope.launch(Dispatchers.IO) {
            var wikipediaContext: WikipediaContext? = null

            // Step 1: Fetch Wikipedia context if toggle is ON
            if (_uiState.value.isWikipediaConnected) {
                try {
                    WikipediaContextProvider.fetchContext(text).collect { status ->
                        _uiState.update { it.copy(wikipediaStatus = status) }
                        if (status is WikipediaSearchStatus.Done) {
                            wikipediaContext = status.context
                        }
                    }
                } catch (_: Exception) {
                    // Ignore Wikipedia fetch errors and proceed to AI response
                }
            }

            // Step 2: Create placeholder assistant message
            val assistantMessage = AiChatMessage(
                role = AiChatMessage.ROLE_ASSISTANT,
                content = "",
                isStreaming = true,
                model = _uiState.value.selectedModel,
                wikipediaContext = wikipediaContext
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    wikipediaStatus = null
                )
            }

            // Step 3: Stream AI response
            val contentBuilder = StringBuilder()
            val thinkingBuilder = StringBuilder()

            try {
                val inputMessages = _uiState.value.messages.dropLast(1)
                AiApiClient.streamChat(
                    model = _uiState.value.selectedModel,
                    messages = inputMessages,
                    wikipediaContext = wikipediaContext
                ).collect { event ->
                    when (event) {
                        is AiApiClient.StreamEvent.Token -> {
                            contentBuilder.append(event.text)
                            updateLastAssistantMessage(
                                content = contentBuilder.toString(),
                                thinkingContent = thinkingBuilder.toString().ifEmpty { null },
                                isStreaming = true
                            )
                        }
                        is AiApiClient.StreamEvent.ThinkingToken -> {
                            thinkingBuilder.append(event.text)
                            _uiState.update {
                                it.copy(currentThinkingContent = thinkingBuilder.toString())
                            }
                        }
                        is AiApiClient.StreamEvent.Error -> {
                            _uiState.update {
                                it.copy(
                                    isStreaming = false,
                                    errorMessage = event.message
                                )
                            }
                            updateLastAssistantMessage(
                                content = contentBuilder.toString().ifEmpty { "⚠️ Error: ${event.message}" },
                                thinkingContent = thinkingBuilder.toString().ifEmpty { null },
                                isStreaming = false
                            )
                        }
                        is AiApiClient.StreamEvent.Done -> {
                            updateLastAssistantMessage(
                                content = contentBuilder.toString(),
                                thinkingContent = thinkingBuilder.toString().ifEmpty { null },
                                isStreaming = false
                            )
                            _uiState.update {
                                it.copy(
                                    isStreaming = false,
                                    currentThinkingContent = ""
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        errorMessage = e.message
                    )
                }
                updateLastAssistantMessage(
                    content = contentBuilder.toString().ifEmpty { "⚠️ ${e.message ?: "Unknown error"}" },
                    isStreaming = false
                )
            }
        }
    }

    private fun updateLastAssistantMessage(
        content: String,
        thinkingContent: String? = null,
        isStreaming: Boolean
    ) {
        _uiState.update { state ->
            val messages = state.messages.toMutableList()
            val lastIndex = messages.lastIndex
            if (lastIndex >= 0 && messages[lastIndex].role == AiChatMessage.ROLE_ASSISTANT) {
                messages[lastIndex] = messages[lastIndex].copy(
                    content = content,
                    thinkingContent = thinkingContent,
                    isStreaming = isStreaming
                )
            }
            state.copy(messages = messages)
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.update { state ->
            val messages = state.messages.toMutableList()
            val lastIndex = messages.lastIndex
            if (lastIndex >= 0 && messages[lastIndex].role == AiChatMessage.ROLE_ASSISTANT) {
                messages[lastIndex] = messages[lastIndex].copy(isStreaming = false)
            }
            state.copy(
                messages = messages,
                isStreaming = false,
                currentThinkingContent = ""
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
