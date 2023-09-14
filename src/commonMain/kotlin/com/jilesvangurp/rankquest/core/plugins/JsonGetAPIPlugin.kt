package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.SearchResults
import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.time.measureTimedValue


class RestStatusException(val status: Int) : Exception("unexpected rest status $status")
class JsonPathError(val path: List<String>) : Exception("json element not found at $path")

@Serializable
data class JsonGetAPIPluginConfig(
    val searchUrl: String,
    val requestHeaders: Map<String, String>,
    val searchContextParams: Map<String, String>,
    val jsonPathToHits: List<String>,
    val jsonPathToId: List<String>,
    val jsonPathToLabel: List<String>?,
    val jsonPathToSize: List<String>?,
)

class JsonGetAPIPluginFactory(
    val httpClient: HttpClient = HttpClient {
        Json(DEFAULT_JSON) { }
    }
) : PluginFactory {
    override fun create(configuration: SearchPluginConfiguration): SearchPlugin {
        val settings = DEFAULT_JSON.decodeFromJsonElement(
            JsonGetAPIPluginConfig.serializer(),
            configuration.pluginSettings ?: error("pluginSettings are required for RestAPIPlugin")
        )
        return JsonGetAPIPlugin(
            httpClient = httpClient,
            searchUrl = settings.searchUrl,
            requestHeaders = settings.requestHeaders,
            jsonPathToHits = settings.jsonPathToHits,
            jsonPathToId = settings.jsonPathToId,
            jsonPathToLabel = settings.jsonPathToLabel,
            jsonPathToSize = settings.jsonPathToSize,
        )
    }
}

/**
 * Simple API Search Plugin that assumes you are calling a search endpoint
 * with some query parameters (your search context) that returns a json object that has a list
 * of hit objects. You provide the json path to the list and then the json path to the id field.
 *
 * A lot of search APIs work like that so you should be able to use this plugin for those APIs. If not, you'll have
 * to implement your own plugin.
 */
class JsonGetAPIPlugin(
    private val httpClient: HttpClient,
    private val searchUrl: String,
    private val requestHeaders: Map<String, String>,
    private val jsonPathToHits: List<String>,
    private val jsonPathToId: List<String>,
    private val jsonPathToLabel: List<String>?,
    private val jsonPathToSize: List<String>?,

    ) : SearchPlugin {
    override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults> {
        val (response, duration) = measureTimedValue {
            httpClient.get(searchUrl) {
                accept(ContentType.Application.Json)
                requestHeaders.forEach { (key, value) ->
                    header(key, value)
                }
                searchContext.forEach { (k, v) ->
                    parameter(k, v)
                }
            }
        }
        return if (response.status.isSuccess()) {
            val obj = DEFAULT_JSON.decodeFromString<JsonObject>(response.bodyAsText())
            val size = jsonPathToSize?.let { obj.get(jsonPathToSize)?.jsonPrimitive?.longOrNull }

            val hits = obj.get(jsonPathToHits)
            if (hits is JsonArray) {
                try {
                    val searchResultList =
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

                    Result.success(SearchResults(size ?: searchResultList.size.toLong(), duration, searchResultList))
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

