package org.wikipedia.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.wikipedia.dataclient.okhttp.OkHttpConnectionFactory
import java.util.concurrent.TimeUnit

data class WikidataFact(
    val propertyName: String,
    val value: String
)

object WikidataFactProvider {

    private val client: OkHttpClient by lazy {
        OkHttpConnectionFactory.client.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Fetches structured Wikidata entity claims & description for a Wikipedia article title.
     */
    suspend fun fetchFacts(title: String, lang: String = "en"): List<WikidataFact> = withContext(Dispatchers.IO) {
        val facts = mutableListOf<WikidataFact>()
        try {
            val url = "https://www.wikidata.org/w/api.php?action=wbgetentities&sites=${lang}wiki&titles=${title}&props=claims|descriptions|labels&languages=${lang}&format=json"

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "InfopediaAlpha/1.0 (Android; Infopedia AI RAG Engine)")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()

            val json = JSONObject(responseBody)
            val entities = json.optJSONObject("entities") ?: return@withContext emptyList()
            val entityId = entities.keys().asSequence().firstOrNull() ?: return@withContext emptyList()
            if (entityId == "-1") return@withContext emptyList()

            val entity = entities.getJSONObject(entityId)

            // Extract Description
            val descriptions = entity.optJSONObject("descriptions")
            val langDesc = descriptions?.optJSONObject(lang)?.optString("value")
            if (!langDesc.isNullOrBlank()) {
                facts.add(WikidataFact("Description", langDesc))
            }

            // Extract Label
            val labels = entity.optJSONObject("labels")
            val langLabel = labels?.optJSONObject(lang)?.optString("value")
            if (!langLabel.isNullOrBlank()) {
                facts.add(WikidataFact("Canonical Name", langLabel))
            }

            // Extract claims summary count
            val claims = entity.optJSONObject("claims")
            if (claims != null) {
                facts.add(WikidataFact("Wikidata Entity ID", entityId))
                facts.add(WikidataFact("Total Claim Attributes", "${claims.length()} verified properties"))
            }
        } catch (_: Exception) {
            // Non-critical fallback
        }
        facts
    }
}
