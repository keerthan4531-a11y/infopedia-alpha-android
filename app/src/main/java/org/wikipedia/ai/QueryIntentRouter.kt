package org.wikipedia.ai

import java.util.Locale

enum class QueryIntent {
    SIMPLE_FACT,
    DEEP_EXPLANATORY
}

object QueryIntentRouter {

    private val SIMPLE_FACT_KEYWORDS = setOf(
        "when", "where", "who", "age", "born", "died", "year", "date",
        "capital", "founder", "ceo", "president", "prime minister",
        "population", "height", "weight", "location", "currency",
        "எப்போது", "எங்கு", "யார்", "வயது", "தலைநகரம்", "ஆண்டு"
    )

    private val DEEP_EXPLANATORY_KEYWORDS = setOf(
        "how", "why", "explain", "history", "process", "evolution",
        "difference", "versus", "vs", "working", "mechanism", "impact",
        "elections", "war", "causes", "effects", "philosophy",
        "எப்படி", "ஏன்", "விளக்கு", "வரலாறு", "வித்தியாசம்"
    )

    /**
     * Classifies user query to determine optimal RAG prompt size and retrieval depth.
     */
    fun classify(query: String): QueryIntent {
        val lower = query.lowercase(Locale.ROOT).trim()
        val words = lower.split(Regex("\\s+"))

        // Short queries under 4 words are likely simple fact checks
        if (words.size <= 4 && words.any { SIMPLE_FACT_KEYWORDS.contains(it) }) {
            return QueryIntent.SIMPLE_FACT
        }

        if (words.any { DEEP_EXPLANATORY_KEYWORDS.contains(it) }) {
            return QueryIntent.DEEP_EXPLANATORY
        }

        // Default: If longer query (> 6 words), default to DEEP_EXPLANATORY, otherwise SIMPLE_FACT
        return if (words.size > 6) QueryIntent.DEEP_EXPLANATORY else QueryIntent.SIMPLE_FACT
    }
}
