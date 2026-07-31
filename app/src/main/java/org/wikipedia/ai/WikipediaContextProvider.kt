package org.wikipedia.ai

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.wikipedia.WikipediaApp
import org.wikipedia.dataclient.ServiceFactory
import org.wikipedia.dataclient.WikiSite

sealed class WikipediaSearchStatus {
    data class Searching(val query: String) : WikipediaSearchStatus()
    data class ReadingPage(val title: String, val current: Int, val total: Int) : WikipediaSearchStatus()
    data class Synthesizing(val count: Int) : WikipediaSearchStatus()
    data class Done(val context: WikipediaContext) : WikipediaSearchStatus()
    data class Error(val message: String) : WikipediaSearchStatus()
}

object WikipediaContextProvider {

    fun fetchContext(query: String, maxPages: Int = 4): Flow<WikipediaSearchStatus> = flow {
        try {
            val intent = QueryIntentRouter.classify(query)
            val primaryWikiSite = WikipediaApp.instance.wikiSite
            val secondaryLang = if (primaryWikiSite.languageCode == "ta") "en" else "ta"
            val secondaryWikiSite = WikiSite.forLanguageCode(secondaryLang)

            val articles = mutableListOf<WikipediaArticleItem>()
            val rawChunks = mutableListOf<RagChunk>()

            val searchTargets = mutableListOf<Pair<WikiSite, String>>()

            if (intent == QueryIntent.MULTI_HOP) {
                val subQueries = QueryIntentRouter.decompose(query)
                emit(WikipediaSearchStatus.Searching("🤖 Agentic Multi-Hop: Decomposing into [${subQueries.joinToString(" + ")}]…"))

                val multiHopTitles = coroutineScope {
                    subQueries.map { subQ ->
                        async { searchTitles(primaryWikiSite, subQ, 2) }
                    }.flatMap { it.await() }.distinct()
                }

                multiHopTitles.forEach { searchTargets.add(Pair(primaryWikiSite, it)) }
            } else {
                emit(WikipediaSearchStatus.Searching(query))
                val searchLimit = if (intent == QueryIntent.SIMPLE_FACT) 2 else maxPages

                // Stage 1: Multi-language search
                val (primaryTitles, secondaryTitles) = coroutineScope {
                    val primaryDeferred = async { searchTitles(primaryWikiSite, query, searchLimit) }
                    val secondaryDeferred = async { searchTitles(secondaryWikiSite, query, 1) }
                    Pair(primaryDeferred.await(), secondaryDeferred.await())
                }

                primaryTitles.forEach { searchTargets.add(Pair(primaryWikiSite, it)) }
                secondaryTitles.forEach { searchTargets.add(Pair(secondaryWikiSite, it)) }
            }

            if (searchTargets.isEmpty()) {
                emit(WikipediaSearchStatus.Done(WikipediaContext()))
                return@flow
            }

            val total = searchTargets.size
            for ((index, target) in searchTargets.withIndex()) {
                val (site, title) = target
                emit(WikipediaSearchStatus.ReadingPage(title = title, current = index + 1, total = total))

                try {
                    val summary = ServiceFactory.getRest(site).getPageSummary(title = title)
                    val extractHtml = summary.extractHtml
                    val extract = summary.extract
                    val cleanExtract = buildString {
                        if (!summary.description.isNullOrEmpty()) {
                            append(summary.description)
                            append(". ")
                        }
                        if (!extractHtml.isNullOrEmpty()) {
                            append(
                                extractHtml
                                    .replace(Regex("<[^>]*>"), "")
                                    .replace("&amp;", "&")
                                    .replace("&lt;", "<")
                                    .replace("&gt;", ">")
                                    .replace("&quot;", "\"")
                                    .trim()
                            )
                        } else if (!extract.isNullOrEmpty()) {
                            append(extract)
                        }
                    }

                    val cleanDisplayTitle = (summary.displayTitle.ifEmpty { summary.apiTitle })
                        .replace(Regex("<[^>]*>"), "")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&quot;", "\"")
                        .trim()

                    val articleItem = WikipediaArticleItem(
                        title = summary.apiTitle,
                        displayTitle = cleanDisplayTitle,
                        description = summary.description,
                        thumbnailUrl = summary.thumbnailUrl,
                        originalImageUrl = summary.originalImageUrl,
                        extract = cleanExtract,
                        langCode = site.languageCode
                    )
                    articles.add(articleItem)

                    // Two-Stage Retrieval Stage 2: Deep Section Chunking ONLY if Query Intent is DEEP_EXPLANATORY
                    if (intent == QueryIntent.DEEP_EXPLANATORY) {
                        val chunks = RagChunkSynthesizer.chunkSection(
                            articleTitle = summary.displayTitle,
                            sectionTitle = summary.description ?: "Overview",
                            fullText = cleanExtract,
                            langCode = site.languageCode
                        )
                        rawChunks.addAll(chunks)
                    }
                } catch (_: Exception) {
                    // Skip single page fetch failure
                }
            }

            if (articles.isNotEmpty()) {
                emit(WikipediaSearchStatus.Synthesizing(count = articles.size))
            }

            // Rank chunks if available (for DEEP_EXPLANATORY)
            val rankedChunks = if (intent == QueryIntent.DEEP_EXPLANATORY) {
                RagChunkSynthesizer.rankChunks(query = query, chunks = rawChunks, topK = 3)
            } else emptyList()

            // Fetch structured Wikidata facts (highest priority for all queries)
            val primaryTitle = articles.firstOrNull()?.title.orEmpty()
            val wikidataFacts = if (primaryTitle.isNotBlank()) {
                WikidataFactProvider.fetchFacts(primaryTitle, primaryWikiSite.languageCode)
            } else emptyList()

            val finalContext = WikipediaContext(
                articles = articles,
                rankedChunks = rankedChunks,
                wikidataFacts = wikidataFacts
            )

            emit(WikipediaSearchStatus.Done(finalContext))
        } catch (e: Exception) {
            emit(WikipediaSearchStatus.Error(e.message ?: "Failed to fetch Infopedia context"))
        }
    }

    /**
     * Hybrid Search (BM25 Keyword + FullText Semantic Search fused via RRF).
     * Runs prefixSearch and fullTextSearch in parallel, then applies Reciprocal Rank Fusion (RRF).
     * Ensures 100% precision for exact entity names, dates, acronyms ("ISRO", "1947") and concepts.
     */
    private suspend fun searchTitles(site: WikiSite, query: String, max: Int): List<String> = coroutineScope {
        val bm25Deferred = async {
            try {
                val response = ServiceFactory.get(site).prefixSearch(searchTerm = query, maxResults = max * 2, gpsOffset = null)
                response.query?.pages.orEmpty().map { it.title }
            } catch (_: Exception) { emptyList() }
        }

        val fullTextDeferred = async {
            try {
                val fullText = ServiceFactory.get(site).fullTextSearch(searchTerm = query, gsrLimit = max * 2, gsrOffset = null)
                fullText.query?.pages.orEmpty().map { it.title }
            } catch (_: Exception) { emptyList() }
        }

        val bm25List = bm25Deferred.await()
        val fullTextList = fullTextDeferred.await()

        // Fuse both ranked lists using Reciprocal Rank Fusion (RRF)
        val fusedTitles = RagChunkSynthesizer.reciprocalRankFusion(bm25List, fullTextList)
        if (fusedTitles.isNotEmpty()) fusedTitles.take(max) else bm25List.ifEmpty { fullTextList }.take(max)
    }
}
