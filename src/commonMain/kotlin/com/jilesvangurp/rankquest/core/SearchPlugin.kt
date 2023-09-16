package com.jilesvangurp.rankquest.core

import com.jilesvangurp.rankquest.core.pluginconfiguration.MetricsOutput
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlin.time.Duration


@Serializable
data class SearchResults(val total: Long, val responseTime: Duration, val searchResultList: List<SearchResult>) {
    @Serializable
    data class SearchResult(val id: String, val label: String? = null)
}


/**
 * The job of a SearchPlugin implementation is to fetch results from any kind of search implementation that
 * given the search context (search parameters) and a number of desired results. It returns a Results
 * instance that specifies the list of results in the order that they appear with their unique Ids and an
 * optional label (for human readbility)
 *
 * A typical implementation would use either ktor client or some kind of client for whatever search client you are
 * using and extract the results.
 *
 */
interface SearchPlugin {
    suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults>
}

suspend fun SearchPlugin.fetchAll(ratedSearches: List<RatedSearch>, k: Int, chunkSize: Int = 4): List<Pair<RatedSearch, SearchResults>> {
    return ratedSearches.chunked(chunkSize).flatMap  {chunk ->
        coroutineScope {
            chunk.map {
                async {
                    it to fetch(it.searchContext,k).getOrThrow()
                }
            }.awaitAll()
        }
    }
}

suspend fun SearchPlugin.runMetrics(
    config: SearchPluginConfiguration,
    ratedSearches: List<RatedSearch>,
    chunkSize: Int = 10
): List<Result<MetricsOutput>> {
    val plugin = this
    return config.metrics.map { metricConfiguration ->
        try {
            val results = metricConfiguration.metric.run(
                searchPlugin = plugin,
                ratedSearches = ratedSearches,
                params = metricConfiguration.params,
                chunkSize = chunkSize
            )
            Result.success(
                MetricsOutput(
                config.name, metricConfiguration, results
            )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

