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
data class RestAPIPluginConfig(
    val searchUrl: String,
    val authorization: String?,
    val jsonPathToHits: List<String>,
    val jsonPathToId: List<String>,
    val jsonPathToLabel: List<String>?,
)

class RestAPIPluginFactory(val httpClient: HttpClient = HttpClient {
    Json(DEFAULT_JSON) {  }
}): PluginFactory {
    override fun create(configuration: SearchPluginConfiguration): SearchPlugin {
        val settings = DEFAULT_JSON.decodeFromJsonElement(RestAPIPluginConfig.serializer(), configuration.pluginSettings)
        return RestAPIPlugin(
            httpClient = httpClient,
            searchUrl = settings.searchUrl,
            authorization = settings.authorization,
            jsonPathToHits = settings.jsonPathToHits,
            jsonPathToId = settings.jsonPathToId,
            jsonPathToLabel = settings.jsonPathToLabel
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
class RestAPIPlugin(
    private val httpClient: HttpClient,
    private val searchUrl: String,
    private val authorization: String?,
    private val jsonPathToHits: List<String>,
    private val jsonPathToId: List<String>,
    private val jsonPathToLabel: List<String>?,
) : SearchPlugin {
    override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults> {
        val response = httpClient.get(searchUrl) {
            accept(ContentType.Application.Json)
            if (!authorization.isNullOrBlank()) {
                header("Authorization", authorization)
            }
            searchContext.forEach { (k, v) ->
                parameter(k, v)
            }
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

fun JsonObject.getString(path: List<String>) = get(path)?.let {
    if (it is JsonPrimitive) it.content else null
}

fun JsonObject.get(path: List<String>): JsonElement? {
    var jsonElement: JsonElement? = this
    for (e in path) {
        if (jsonElement is JsonObject) {
            jsonElement = jsonElement.get(e)
        } else
            jsonElement = null
        break
    }
    return jsonElement
}