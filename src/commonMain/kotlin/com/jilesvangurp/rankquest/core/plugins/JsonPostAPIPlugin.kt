package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.SearchResults
import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.time.measureTimedValue


@Serializable
data class JsonPostAPIPluginConfig(
    val searchUrl: String,
    val requestHeaders: Map<String, String>,
    val requestBodyTemplate: String,
    val jsonPathToHits: List<String>,
    val jsonPathToId: List<String>,
    val jsonPathToLabel: List<String>?,
)

class JsonPostAPIPluginFactory(
    val httpClient: HttpClient = HttpClient {
    Json(DEFAULT_JSON) {  }
}): PluginFactory {
    override fun create(configuration: SearchPluginConfiguration): SearchPlugin {
        val settings = DEFAULT_JSON.decodeFromJsonElement(JsonPostAPIPluginConfig.serializer(), configuration.pluginSettings ?: error("pluginSettings are required for RestAPIPlugin"))
        return JsonPostAPIPlugin(
            httpClient = httpClient,
            searchUrl = settings.searchUrl,
            requestBodyTemplate = settings.requestBodyTemplate,
            requestHeaders = settings.requestHeaders,
            jsonPathToHits = settings.jsonPathToHits,
            jsonPathToId = settings.jsonPathToId,
            jsonPathToLabel = settings.jsonPathToLabel
        )
    }
}

fun String.applySearchContext(searchContext: Map<String,String>): String {
    var templated=this
    searchContext.forEach { (key,value) ->
        val regex = "\\{\\{\\s*$key\\s*\\}\\}".toRegex()
        templated=templated.replace(regex,value)
    }
    return templated
}

/**
 * Simple API Search Plugin that assumes you are calling a search endpoint
 * with some query parameters (your search context) that returns a json object that has a list
 * of hit objects. You provide the json path to the list and then the json path to the id field.
 *
 * A lot of search APIs work like that so you should be able to use this plugin for those APIs. If not, you'll have
 * to implement your own plugin.
 */
class JsonPostAPIPlugin(
    private val httpClient: HttpClient,
    private val searchUrl: String,
    private val requestHeaders: Map<String,String>,
    private val requestBodyTemplate: String,
    private val jsonPathToHits: List<String>,
    private val jsonPathToId: List<String>,
    private val jsonPathToLabel: List<String>?,
) : SearchPlugin {
    override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults> {
        val response = httpClient.post(searchUrl) {
            requestHeaders.forEach {(key,value) ->
                header(key,value)
            }
            accept(ContentType.Application.Json)
            setBody(TextContent(requestBodyTemplate.applySearchContext(searchContext),ContentType.Application.Json))
        }
        return if (response.status.isSuccess()) {
            val obj = DEFAULT_JSON.decodeFromString<JsonObject>(response.bodyAsText())

            val hits = obj.get(jsonPathToHits)
            if (hits is JsonArray) {
                try {
                    val (searchResultList, responseTime) = measureTimedValue {
                        hits.map { hit ->
                            if (hit is JsonObject) {
                                val id = hit.getString(jsonPathToId)
                                if (id != null) {
                                    SearchResults.SearchResult(
                                        id,
                                        jsonPathToLabel?.let { hit.getString(jsonPathToLabel) })
                                } else {
                                    throw JsonPathError(jsonPathToId)
                                }
                            } else throw JsonPathError(jsonPathToHits)
                        }
                    }
                    Result.success(SearchResults(searchResultList.size.toLong(), responseTime, searchResultList))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(JsonPathError(jsonPathToHits))
            }
        } else {
            Result.failure(RestStatusException(response.status.value))
        }
    }
}

