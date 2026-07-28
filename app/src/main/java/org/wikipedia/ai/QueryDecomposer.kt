package org.wikipedia.ai

sealed class QueryPlan {
    data class SingleQuery(val query: String) : QueryPlan()
    data class MultiHopQuery(val subQueries: List<String>, val originalQuery: String) : QueryPlan()
}

object QueryDecomposer {

    private val COMPARATIVE_KEYWORDS = setOf(
        "and", "versus", "vs", "between", "relationship", "relation", "compare", "compared", "connection",
        "மற்றும்", "தொடர்பு", "வித்தியாசம்", "ஒப்பீடு", "இணைப்பு", "இருவருக்கும்"
    )

    /**
     * Fast, lightweight query planner.
     * Determines whether the user query requires multi-entity decomposition or single-topic search.
     * ZERO latency overhead for simple queries.
     */
    fun plan(query: String): QueryPlan {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return QueryPlan.SingleQuery(trimmed)

        val words = trimmed.lowercase().split(Regex("\\s+"))

        // Fast check: if query has fewer than 4 words or has no comparative/relational keywords, return SingleQuery immediately
        if (words.size < 4 || words.none { COMPARATIVE_KEYWORDS.contains(it) }) {
            return QueryPlan.SingleQuery(trimmed)
        }

        // Decompose multi-entity query into distinct sub-queries (max 3)
        val splitRegex = Regex("(?i)\\b(and|versus|vs|difference between|connection between|relationship between|relation between|compared to|மற்றும்|தொடர்பு|வித்தியாசம்|ஒப்பீடு|இணைப்பு|இருவருக்கும்)\\b")
        val parts = trimmed.split(splitRegex)
            .map { part ->
                part.replace(Regex("(?i)\\b(what|is|the|of|between|in|both|two|iruvarkum|ullathu|ennu|ennavendral|connection|relationship|difference|compare)\\b"), "").trim()
            }
            .filter { it.length >= 2 }

        if (parts.size >= 2) {
            val subQueries = parts.take(3)
            return QueryPlan.MultiHopQuery(
                subQueries = subQueries,
                originalQuery = trimmed
            )
        }

        return QueryPlan.SingleQuery(trimmed)
    }
}
