package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.SearchResults
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import com.jillesvangurp.ktsearch.KtorRestClient
import com.jillesvangurp.ktsearch.SearchClient
import com.jillesvangurp.ktsearch.search
import com.jillesvangurp.ktsearch.total
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class ElasticsearchPluginConfiguration(
    val queryTemplate: String,
    val index: String,
    val labelFields: List<String>? = null,
    val host: String = "localhost",
    val port: Int = 9200,
    val https: Boolean = false,
    val user: String? = null,
    val password: String? = null,
    val logging: Boolean = false,
)

class ElasticsearchPluginFactory() : PluginFactory {
    override fun create(configuration: SearchPluginConfiguration): SearchPlugin {
        val pluginSettings = configuration.pluginSettings?.let<JsonObject, ElasticsearchPluginConfiguration> {
            DEFAULT_JSON.decodeFromJsonElement(it)
        } ?: error("pluginSettings are missing")
        return ElasticsearchPlugin(pluginSettings)
    }
}

class ElasticsearchPlugin(val configuration: ElasticsearchPluginConfiguration) : SearchPlugin {

    val client by lazy {
        SearchClient(
            KtorRestClient(
                host = configuration.host,
                port = configuration.port,
                https = configuration.https,
                user = configuration.user,
                password = configuration.password,
                logging = configuration.logging
            )
        )
    }

    override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults> {
        val query = configuration.queryTemplate.applySearchContext(searchContext)
        return try {
            client.search(configuration.index, query).let {
                SearchResults(it.total, it.took?.milliseconds ?: ZERO, it.hits?.hits?.map { hit ->
                    SearchResults.SearchResult(hit.id, configuration.labelFields?.map { field ->
                        hit.source?.get(field)?.jsonPrimitive?.content ?: "-"
                    }?.joinToString(" | "))
                }.orEmpty())
            }.let {
                Result.success(it)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}