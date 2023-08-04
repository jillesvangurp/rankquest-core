package com.jilesvangurp.ktranker

import kotlinx.serialization.Serializable
import kotlin.math.log2

@Serializable
enum class Metric {
    PrecisionAtK,
    RecallAtK,
    MeanReciprocalRank,
    DiscountedCumulativeGain,
}

suspend fun SearchPlugin.run(
    metric: Metric,
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    relevantRatingThreshold: Int = 1
) = when (metric) {
    Metric.PrecisionAtK -> precisionAtK(
        ratedSearches = ratedSearches,
        k = k,
        relevantRatingThreshold = relevantRatingThreshold
    )

    Metric.RecallAtK -> recallAtK(
        ratedSearches = ratedSearches,
        k = k,
        relevantRatingThreshold = relevantRatingThreshold
    )

    Metric.MeanReciprocalRank -> meanReciprocalRank(
        ratedSearches = ratedSearches,
        k = k,
        relevantRatingThreshold = relevantRatingThreshold,
    )

    Metric.DiscountedCumulativeGain -> discountedCumulativeGain(
        ratedSearches = ratedSearches,
        k = k,
        relevantRatingThreshold = relevantRatingThreshold
    )
}

suspend fun SearchPlugin.runAllMetrics(ratedSearches: List<RatedSearch>, k: Int = 5, relevantRatingThreshold: Int = 1) =
    Metric.entries.map {
        it to run(
            metric = it,
            ratedSearches = ratedSearches,
            k = k,
            relevantRatingThreshold = relevantRatingThreshold
        )
    }

suspend fun SearchPlugin.precisionAtK(
    ratedSearches: List<RatedSearch>,
    relevantRatingThreshold: Int = 1,
    k: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k) }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        val relevantCount = results.resultList.take(k).count { result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val isRelevant = rating.rating >= relevantRatingThreshold
                if (isRelevant) {
                    hits.add(result.id to 1.0)
                } else {
                    unRated.add(result.id)
                }
                isRelevant
            } else {
                unRated.add(result.id)
                false
            }
        }

        val precision = relevantCount.toDouble() / k
        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = precision,
            unRated = unRated,
            hits = hits
        )
    }

    val globalPrecisionAtK = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalPrecisionAtK, metricResults)
}

suspend fun SearchPlugin.recallAtK(
    ratedSearches: List<RatedSearch>,
    relevantRatingThreshold: Int = 1,
    k: Int,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k) }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        val relevantCount = results.resultList.take(k).count { result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val isRelevant = rating.rating >= relevantRatingThreshold
                if (isRelevant) {
                    hits.add(result.id to 1.0)
                } else {
                    unRated.add(result.id)
                }
                isRelevant
            } else {
                unRated.add(result.id)
                false
            }
        }

        val totalRelevant = ratedSearch.ratings.count { it.rating >= relevantRatingThreshold }
        val recall = if (totalRelevant > 0) relevantCount.toDouble() / totalRelevant else 0.0

        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = recall,
            hits = hits,
            unRated = unRated
        )
    }

    val globalRecallAtK = metricResults.map { it.metric }.sum() / metricResults.size
    return MetricResults(globalRecallAtK, metricResults)
}


suspend fun SearchPlugin.meanReciprocalRank(
    ratedSearches: List<RatedSearch>,
    k: Int,
    relevantRatingThreshold: Int = 1,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k) }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()
        val sum = results.resultList.mapNotNull { result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val reciprocal = if (rating.rating >= relevantRatingThreshold) {
                    1.0 / rating.rating
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
        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = sum,
            unRated = unRated,
            hits = hits
        )
    }

    val meanReciprocalRank = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(meanReciprocalRank, metricResults)
}

suspend fun SearchPlugin.discountedCumulativeGain(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    relevantRatingThreshold: Int = 1,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k) }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        val dcg = results.resultList.mapIndexedNotNull { index, result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val isRelevant = rating.rating >= relevantRatingThreshold
                if (!isRelevant) unRated.add(result.id)

                val position = index + 1
                val discountFactor = if (position == 1) 1.0 else log2(position.toDouble())
                val gain = if (isRelevant) rating.rating / discountFactor else 0.0
                hits.add(result.id to gain)
                gain
            } else {
                unRated.add(result.id)
                null
            }
        }.sum()

        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = dcg,
            hits = hits,
            unRated = unRated
        )
    }

    val globalDCG = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalDCG, metricResults)
}