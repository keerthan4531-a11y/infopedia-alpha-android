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
        emit(WikipediaSearchStatus.Searching(query))

        try {
            val primaryWikiSite = WikipediaApp.instance.wikiSite
            val secondaryLang = if (primaryWikiSite.languageCode == "ta") "en" else "ta"
            val secondaryWikiSite = WikiSite.forLanguageCode(secondaryLang)

            val articles = mutableListOf<WikipediaArticleItem>()
            val rawChunks = mutableListOf<RagChunk>()

            // Step 1: Multi-language parallel search
            val (primaryTitles, secondaryTitles) = coroutineScope {
                val primaryDeferred = async { searchTitles(primaryWikiSite, query, maxPages) }
                val secondaryDeferred = async { searchTitles(secondaryWikiSite, query, 2) }
                Pair(primaryDeferred.await(), secondaryDeferred.await())
            }

            val searchTargets = mutableListOf<Pair<WikiSite, String>>()
            primaryTitles.forEach { searchTargets.add(Pair(primaryWikiSite, it)) }
            secondaryTitles.forEach { searchTargets.add(Pair(secondaryWikiSite, it)) }

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

                    val articleItem = WikipediaArticleItem(
                        title = summary.apiTitle,
                        displayTitle = summary.displayTitle,
                        description = summary.description,
                        thumbnailUrl = summary.thumbnailUrl,
                        originalImageUrl = summary.originalImageUrl,
                        extract = cleanExtract,
                        langCode = site.languageCode
                    )
                    articles.add(articleItem)

                    // Chunk article content into sections for deep semantic RAG
                    val chunks = RagChunkSynthesizer.chunkSection(
                        articleTitle = summary.displayTitle,
                        sectionTitle = summary.description ?: "Overview",
                        fullText = cleanExtract,
                        langCode = site.languageCode
                    )
                    rawChunks.addAll(chunks)
                } catch (_: Exception) {
                    // Skip single page fetch failure
                }
            }

            if (articles.isNotEmpty()) {
                emit(WikipediaSearchStatus.Synthesizing(count = articles.size))
            }

            // Step 2: Rank chunks with keyword relevance scoring algorithm
            val rankedChunks = RagChunkSynthesizer.rankChunks(query = query, chunks = rawChunks, topK = 6)

            // Step 3: Fetch structured Wikidata facts for primary topic
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
            emit(WikipediaSearchStatus.Error(e.message ?: "Failed to fetch Wikipedia context"))
        }
    }

    private suspend fun searchTitles(site: WikiSite, query: String, max: Int): List<String> {
        return try {
            val response = ServiceFactory.get(site).prefixSearch(searchTerm = query, maxResults = max, gpsOffset = null)
            var titles = response.query?.pages.orEmpty().map { it.title }
            if (titles.isEmpty()) {
                val fullText = ServiceFactory.get(site).fullTextSearch(searchTerm = query, gsrLimit = max, gsrOffset = null)
                titles = fullText.query?.pages.orEmpty().map { it.title }
            }
            titles
        } catch (_: Exception) {
            emptyList()
        }
    }
}
