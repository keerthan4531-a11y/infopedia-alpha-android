package org.wikipedia.ai

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
            val wikiSite = WikipediaApp.instance.wikiSite

            // Step 1: Prefix search
            val searchResponse = ServiceFactory.get(wikiSite).prefixSearch(
                searchTerm = query,
                maxResults = maxPages,
                gpsOffset = null
            )

            var titles = searchResponse.query?.pages.orEmpty().map { it.title }

            if (titles.isEmpty()) {
                // Fallback to full-text search
                val fullTextResponse = ServiceFactory.get(wikiSite).fullTextSearch(
                    searchTerm = query,
                    gsrLimit = maxPages,
                    gsrOffset = null
                )
                titles = fullTextResponse.query?.pages.orEmpty().map { it.title }
            }

            if (titles.isEmpty()) {
                emit(WikipediaSearchStatus.Done(WikipediaContext()))
                return@flow
            }

            val articles = mutableListOf<WikipediaArticleItem>()
            val total = titles.size

            for ((index, title) in titles.withIndex()) {
                emit(WikipediaSearchStatus.ReadingPage(title = title, current = index + 1, total = total))

                try {
                    val summary = ServiceFactory.getRest(wikiSite).getPageSummary(title = title)
                    val extractHtml = summary.extractHtml
                    val extract = summary.extract
                    val cleanExtract = buildString {
                        if (!summary.description.isNullOrEmpty()) {
                            append(summary.description)
                            append(". ")
                        }
                        if (!extractHtml.isNullOrEmpty()) {
                            append(extractHtml
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

                    articles.add(
                        WikipediaArticleItem(
                            title = summary.apiTitle,
                            displayTitle = summary.displayTitle,
                            description = summary.description,
                            thumbnailUrl = summary.thumbnailUrl,
                            originalImageUrl = summary.originalImageUrl,
                            extract = cleanExtract
                        )
                    )
                } catch (_: Exception) {
                    // Skip page fetch errors
                }
            }

            if (articles.isNotEmpty()) {
                emit(WikipediaSearchStatus.Synthesizing(count = articles.size))
            }

            emit(WikipediaSearchStatus.Done(WikipediaContext(articles)))
        } catch (e: Exception) {
            emit(WikipediaSearchStatus.Error(e.message ?: "Failed to fetch Wikipedia context"))
        }
    }
}
