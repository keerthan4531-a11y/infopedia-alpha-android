package org.wikipedia.ai

import java.util.UUID

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val isStreaming: Boolean = false,
    val model: AiModel? = null,
    val wikipediaContext: WikipediaContext? = null,
    val thinkingContent: String? = null
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val ROLE_SYSTEM = "system"
    }
}

data class WikipediaArticleItem(
    val title: String,
    val displayTitle: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val originalImageUrl: String? = null,
    val extract: String? = null,
    val langCode: String = "en",
    val sectionName: String? = null
)

data class WikipediaContext(
    val articles: List<WikipediaArticleItem> = emptyList(),
    val rankedChunks: List<RagChunk> = emptyList(),
    val wikidataFacts: List<WikidataFact> = emptyList()
) {
    val primaryArticle: WikipediaArticleItem? get() = articles.firstOrNull { !it.originalImageUrl.isNullOrEmpty() || !it.thumbnailUrl.isNullOrEmpty() } ?: articles.firstOrNull()
    val pagesRead: List<String> get() = articles.map { it.displayTitle }.distinct()
    val summaries: List<String> get() = articles.map { it.extract.orEmpty() }
}
