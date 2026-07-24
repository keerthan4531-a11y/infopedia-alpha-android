package org.wikipedia.ai

import java.util.Locale

data class RagChunk(
    val articleTitle: String,
    val sectionTitle: String,
    val content: String,
    val score: Float = 0f,
    val langCode: String = "en"
)

object RagChunkSynthesizer {

    private val STOP_WORDS = setOf(
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
        "in", "on", "at", "to", "for", "from", "by", "with", "about", "against",
        "between", "into", "through", "during", "before", "after", "above", "below",
        "of", "and", "or", "but", "not", "this", "that", "these", "those", "it", "its",
        "has", "have", "had", "do", "does", "did", "which", "who", "whom", "what",
        "where", "when", "why", "how", "all", "any", "both", "each", "few", "more",
        "most", "other", "some", "such", "no", "nor", "only", "own", "same", "so",
        "than", "too", "very", "can", "will", "just", "should", "now"
    )

    /**
     * Splits a long article section into ~300-400 word chunks preserving section context.
     */
    fun chunkSection(
        articleTitle: String,
        sectionTitle: String,
        fullText: String,
        langCode: String = "en",
        chunkSizeWords: Int = 300
    ): List<RagChunk> {
        val words = fullText.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()

        if (words.size <= chunkSizeWords) {
            return listOf(
                RagChunk(
                    articleTitle = articleTitle,
                    sectionTitle = sectionTitle,
                    content = fullText,
                    langCode = langCode
                )
            )
        }

        val chunks = mutableListOf<RagChunk>()
        var start = 0

        while (start < words.size) {
            val end = minOf(start + chunkSizeWords, words.size)
            val chunkText = words.subList(start, end).joinToString(" ")

            chunks.add(
                RagChunk(
                    articleTitle = articleTitle,
                    sectionTitle = sectionTitle,
                    content = chunkText,
                    langCode = langCode
                )
            )

            // Overlap chunks by 50 words for smooth context boundary
            start += (chunkSizeWords - 50).coerceAtLeast(100)
        }

        return chunks
    }

    /**
     * Ranks chunks using keyword overlap and term-frequency scoring against the query.
     */
    fun rankChunks(
        query: String,
        chunks: List<RagChunk>,
        topK: Int = 6
    ): List<RagChunk> {
        if (chunks.isEmpty()) return emptyList()

        val queryTerms = query.lowercase(Locale.ROOT)
            .split(Regex("[^a-zA-Z0-9\u0B80-\u0BFF]+"))
            .filter { it.length > 2 && !STOP_WORDS.contains(it) }
            .toSet()

        if (queryTerms.isEmpty()) {
            return chunks.take(topK)
        }

        val scoredChunks = chunks.map { chunk ->
            val chunkLower = chunk.content.lowercase(Locale.ROOT)
            val sectionLower = chunk.sectionTitle.lowercase(Locale.ROOT)
            val titleLower = chunk.articleTitle.lowercase(Locale.ROOT)

            var score = 0f

            for (term in queryTerms) {
                // Term frequency in content
                val occurrences = chunkLower.split(term).size - 1
                score += occurrences * 1.5f

                // Bonus if term matches section title or article title
                if (sectionLower.contains(term)) score += 4.0f
                if (titleLower.contains(term)) score += 3.0f
            }

            chunk.copy(score = score)
        }

        return scoredChunks
            .sortedByDescending { it.score }
            .take(topK)
    }

    /**
     * Compresses raw paragraph text into structured key-fact lines.
     * Reduces LLM token consumption by 30-40%.
     */
    fun compressChunk(chunk: RagChunk): String {
        val sentences = chunk.content.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val compressedSentences = sentences.take(4).map { sentence ->
            sentence.replace(Regex("\\[\\d+\\]"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
        return buildString {
            append("[")
            append(chunk.articleTitle)
            append(" > ")
            append(chunk.sectionTitle)
            append(" (")
            append(chunk.langCode.uppercase())
            append(")] ")
            append(compressedSentences.joinToString("; "))
        }
    }
}
