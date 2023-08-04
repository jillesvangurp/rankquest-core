package com.jilesvangurp.ktranker

import kotlinx.serialization.Serializable
import kotlin.time.Duration


@Serializable
data class Results(val total: Long, val responseTime: Duration, val resultList: List<Result>) {
    @Serializable
    data class Result(val id: String, val label: String? = null)
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
    suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Results
}

data class RatedSearch(
    val id: String,
    val searchContext: Map<String, String>,
    val ratings: Map<String, Int>
)

data class MetricResults(
    val metric: Double,
    val details: List<MetricResult>
) {
    data class MetricResult(
        val id: String,
        val metric: Double,
        val hits: List<Pair<String, Double>>,
        val unRated: List<String>
    )
}
