package com.jilesvangurp.ktranker

import kotlin.time.Duration


data class Result(val id: String, val label: String? = null)
data class Results(val total: Long, val responseTime: Duration, val resultList: List<Result>)

interface SearchPlugin {
    suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch:Int): Results
}

data class RatedSearch(
    val id: String,
    val searchContext: Map<String, String>,
    val ratings: Map<String, Int>
)

data class MetricResults(
    val metric: Double,
    val details: List<MetricResult>
)

data class MetricResult(
    val id: String,
    val metric: Double,
    val hits: List<Pair<String, Double>>,
    val unRated: List<String>
)

suspend fun SearchPlugin.meanReciprocalRank(
    ratedSearches: List<RatedSearch>,
    relevantRatingThreshold: Int = 1,
    numberOfItemsToFetch: Int,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, numberOfItemsToFetch) }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()
        val sum = results.resultList.mapNotNull { result ->
            val i = ratedSearch.ratings[result.id]
            if (i != null) i.let { rank ->
                val reciprocal = if (rank >= relevantRatingThreshold) {
                    1.0 / rank
                } else {
                    0.0
                }
                hits.add(result.id to reciprocal)
                reciprocal
            } else {
                unRated.add(result.id)
                null
            }
        }.sum()
        MetricResult(
            id = ratedSearch.id,
            metric = sum,
            unRated = unRated,
            hits = hits
        )
    }

    val meanReciprocalRank = metricResults.map { it.metric }.sum() / metricResults.size
    return MetricResults(meanReciprocalRank, metricResults)
}

