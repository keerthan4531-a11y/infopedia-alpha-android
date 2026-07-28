package org.wikipedia.ai

import java.util.Locale

enum class QueryIntent {
    SIMPLE_FACT,
    DEEP_EXPLANATORY,
    MULTI_HOP
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

    private val MULTI_HOP_KEYWORDS = setOf(
        "and", "versus", "vs", "between", "relationship", "relation", "compare", "compared",
        "மற்றும்", "தொடர்பு", "வித்தியாசம்", "ஒப்பீடு", "இணைப்பு"
    )

    /**
     * Classifies user query to determine optimal RAG prompt size, multi-hop decomposition, and retrieval depth.
     */
    fun classify(query: String): QueryIntent {
        val lower = query.lowercase(Locale.ROOT).trim()
        val words = lower.split(Regex("\\s+"))

        // Check if query is Multi-Hop (involves multiple entities/topics connected by "and", "vs", "relationship")
        if (words.any { MULTI_HOP_KEYWORDS.contains(it) } && words.size >= 4) {
            val subQueries = decompose(query)
            if (subQueries.size >= 2) {
                return QueryIntent.MULTI_HOP
            }
        }

        // Short queries under 4 words are likely simple fact checks
        if (words.size <= 4 && words.any { SIMPLE_FACT_KEYWORDS.contains(it) }) {
            return QueryIntent.SIMPLE_FACT
        }

        if (words.any { DEEP_EXPLANATORY_KEYWORDS.contains(it) }) {
            return QueryIntent.DEEP_EXPLANATORY
        }

        return if (words.size > 6) QueryIntent.DEEP_EXPLANATORY else QueryIntent.SIMPLE_FACT
    }

    /**
     * Decomposes a complex multi-entity query into distinct sub-queries for Agentic Multi-Hop RAG fetching.
     * Example: "அப்துல் கலாம் மற்றும் சி.வி. ராமன் தொடர்பு" -> ["அப்துல் கலாம்", "சி.வி. ராமன்"]
     */
    fun decompose(query: String): List<String> {
        val splitRegex = Regex("(?i)\\b(and|versus|vs|difference between|relationship between|relation between|compared to|மற்றும்|தொடர்பு|வித்தியாசம்|ஒப்பீடு|இணைப்பு)\\b")
        val parts = query.split(splitRegex)
            .map { it.replace(Regex("(?i)\\b(what|is|the|of|between|in|both|two|iruvarkum|ullathu|ennu|ennavendral)\\b"), "").trim() }
            .filter { it.length >= 3 }

        return if (parts.size >= 2) parts.take(3) else listOf(query)
    }
}
