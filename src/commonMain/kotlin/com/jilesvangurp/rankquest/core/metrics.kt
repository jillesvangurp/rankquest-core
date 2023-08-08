package com.jilesvangurp.rankquest.core

import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
enum class Metric {
    PrecisionAtK,
    RecallAtK,
    MeanReciprocalRank,
    ExpectedReciprocalRank,
    DiscountedCumulativeGain,
}

suspend fun SearchPlugin.run(
    metric: Metric,
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    relevantRatingThreshold: Int = 1,
    maxRelevance: Int = 5,
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

    Metric.ExpectedReciprocalRank -> expectedMeanReciprocalRank(ratedSearches, maxRelevance = maxRelevance)

    Metric.DiscountedCumulativeGain -> discountedCumulativeGain(
        ratedSearches = ratedSearches,
        k = k
    )
}

suspend fun SearchPlugin.runAllMetrics(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    relevantRatingThreshold: Int = 1,
    maxRelevance: Int = 5, // err only
) =
    Metric.entries.map {
        it to run(
            metric = it,
            ratedSearches = ratedSearches,
            k = k,
            relevantRatingThreshold = relevantRatingThreshold,
            maxRelevance = maxRelevance
        )
    }

suspend fun SearchPlugin.precisionAtK(
    ratedSearches: List<RatedSearch>,
    relevantRatingThreshold: Int = 1,
    k: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        val relevantCount = results.searchResultList.take(k).count { result ->
            val rating = ratedSearch.ratings[result.id]?.rating
            if (rating != null) {
                val isRelevant = rating >= relevantRatingThreshold
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
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        val relevantCount = results.searchResultList.take(k).count { result ->
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
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()
        val sum = results.searchResultList.mapNotNull { result ->
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

suspend fun SearchPlugin.expectedMeanReciprocalRank(
    ratedSearches: List<RatedSearch>,
    maxRelevance: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, ratedSearches.size).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<String>()
        val hits = mutableListOf<Pair<String, Double>>()

        var p = 1.0
        var rank = 1

        val powMaxRelevance = 2.0.pow(maxRelevance)
        val err = results.searchResultList.mapIndexedNotNull { index, result ->
            val rating = ratedSearch.ratings[result.id]?.rating?.let {
                // normalize things to maxRelevance if they rare higher
                min(it, maxRelevance)
            }
            if (rating != null) {
                val probabilityForRating = (2.0.pow(rating) - 1.0) / powMaxRelevance

                val errLocal = p * probabilityForRating / rank
                p *= (1.0 - probabilityForRating)
                errLocal
            } else {
                unRated.add(result.id)
                null
            }.also {
                rank++
            }
        }.sum()

        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = err,
            hits = hits,
            unRated = unRated
        )
    }
    val globalERR = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalERR, metricResults)
}

private fun dcg(rs: List<Int?>): Double {
    var rank = 1
    var dcg = 0.0
    for (r in rs) {
        if (r != null && r != 0) {
            dcg += (2.0.pow(r) - 1) / ((ln((rank + 1).toDouble()) / ln(2.0)))
        }
        rank++
    }
    return dcg
}

suspend fun SearchPlugin.discountedCumulativeGain(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val hits = mutableListOf<Pair<String, Double>>()

        val unRated = results.searchResultList.filter { ratedSearch.ratings[it.id] == null }.map { it.id }
        val rs = results.searchResultList.map { result -> ratedSearch.ratings[result.id]?.rating }

        val dcg = dcg(rs)

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
suspend fun SearchPlugin.normalizedDiscountedCumulativeGain(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val hits = mutableListOf<Pair<String, Double>>()

        val unRated = results.searchResultList.filter { ratedSearch.ratings[it.id] == null }.map { it.id }
        val rs = results.searchResultList.map { result -> ratedSearch.ratings[result.id]?.rating }

        val dcg = dcg(rs)

        val allRatings = ratedSearch.ratings.map { it.rating }.reversed()
        val idcg = dcg(allRatings.subList(0, min(allRatings.size, rs.size)))
        val normalized = if (idcg == 0.0) {
            0.0
        } else {
            dcg / idcg
        }
        MetricResults.MetricResult(
            id = ratedSearch.id,
            metric = normalized,
            hits = hits,
            unRated = unRated
        )
    }

    val globalDCG = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalDCG, metricResults)
}