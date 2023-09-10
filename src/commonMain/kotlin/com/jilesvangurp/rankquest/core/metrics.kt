package com.jilesvangurp.rankquest.core

import com.jilesvangurp.rankquest.core.pluginconfiguration.MetricParam
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow


interface MetricImplementation {
    suspend fun evaluate(
        searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
    ): MetricResults
}

suspend fun SearchPlugin.precisionAtK(
    ratedSearches: List<RatedSearch>,
    relevantRatingThreshold: Int = 1,
    k: Int = 5,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<MetricResults.DocumentReference>()
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()

        val relevantCount = results.searchResultList.take(k).count { result ->
            val rating = ratedSearch.ratings[result.id]?.rating
            if (rating != null) {
                val isRelevant = rating >= relevantRatingThreshold
                if (isRelevant) {
                    hits.add(MetricResults.DocumentReference(result.id, result.label) to 1.0)
                } else {
                    unRated.add(MetricResults.DocumentReference(result.id, result.label))
                }
                isRelevant
            } else {
                unRated.add(MetricResults.DocumentReference(result.id, result.label))
                false
            }
        }

        val precision = relevantCount.toDouble() / k
        MetricResults.MetricResult(
            id = ratedSearch.id, metric = precision, unRated = unRated, hits = hits
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
        val unRated = mutableListOf<MetricResults.DocumentReference>()
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()

        val relevantCount = results.searchResultList.take(k).count { result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val isRelevant = rating.rating >= relevantRatingThreshold
                if (isRelevant) {
                    hits.add(MetricResults.DocumentReference(result.id, result.label) to 1.0)
                } else {
                    unRated.add(MetricResults.DocumentReference(result.id, result.label))
                }
                isRelevant
            } else {
                unRated.add(MetricResults.DocumentReference(result.id, result.label))
                false
            }
        }

        val expectedRelevantDocuments = ratedSearch.ratings.count { it.rating >= relevantRatingThreshold }
        val recall = if (expectedRelevantDocuments > 0) relevantCount.toDouble() / expectedRelevantDocuments else 0.0

        MetricResults.MetricResult(
            id = ratedSearch.id, metric = recall, hits = hits, unRated = unRated
        )
    }

    val globalRecallAtK = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalRecallAtK, metricResults)
}


suspend fun SearchPlugin.meanReciprocalRank(
    ratedSearches: List<RatedSearch>,
    k: Int,
    relevantRatingThreshold: Int = 1,
): MetricResults {
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val unRated = mutableListOf<MetricResults.DocumentReference>()
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()
        var position=1

        val reciprocal = results.searchResultList.map { result ->
            val rating = ratedSearch.ratings[result.id]
            if (rating != null) {
                val reciprocal = if (rating.rating >= relevantRatingThreshold) {
                    1.0 / position
                } else {
                    0.0
                }
                hits.add(MetricResults.DocumentReference(result.id, result.label) to reciprocal)
                reciprocal

            } else {
                unRated.add(MetricResults.DocumentReference(result.id, result.label))
                0.0
            }.also {
                position++
            }
        }.firstOrNull {it >0} ?: 0.0
        MetricResults.MetricResult(
            id = ratedSearch.id, metric = reciprocal, unRated = unRated, hits = hits
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
        val unRated = mutableListOf<MetricResults.DocumentReference>()
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()

        var p = 1.0
        var position = 1

        val powMaxRelevance = 2.0.pow(maxRelevance)
        val err = results.searchResultList.mapIndexedNotNull { _, result ->
            val rating = ratedSearch.ratings[result.id]?.rating?.let {
                // normalize things to maxRelevance if they rare higher
                min(it, maxRelevance)
            }
            if (rating != null) {
                val probabilityForRating = (2.0.pow(rating) - 1.0) / powMaxRelevance

                val errForHit = p * probabilityForRating / position
                p *= (1.0 - probabilityForRating)
                hits.add(MetricResults.DocumentReference(result.id, result.label) to errForHit)
                errForHit

            } else {
                unRated.add(MetricResults.DocumentReference(result.id, result.label))
                null
            }.also {
                position++
            }
        }.sum()

        MetricResults.MetricResult(
            id = ratedSearch.id, metric = err, hits = hits, unRated = unRated
        )
    }
    val globalERR = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(globalERR, metricResults)
}

private fun dcgExponential(
    rs: List<Pair<MetricResults.DocumentReference, Int?>>,
    hits: MutableList<Pair<MetricResults.DocumentReference, Double>>
): Double {
    // loosely based on what elastic does, not sure why they divide by ln(2.0)
    var rank = 1
    var dcg = 0.0
    rs.forEach { (documentReference, r) ->
        if (r != null && r != 0) {
            val dcgLocal = (2.0.pow(r) - 1) / ((ln((rank + 1).toDouble()) / ln(2.0)))
            hits.add(documentReference to dcgLocal)
            dcg += dcgLocal
        }
        rank++
    }
    return dcg
}

private fun linearDcg(
    hitsWithRating: List<Pair<MetricResults.DocumentReference, Int?>>,
    hits: MutableList<Pair<MetricResults.DocumentReference, Double>>
): Double {
    var position = 1
    var dcg = 0.0
    hitsWithRating.forEach { (documentReference, rating) ->
        if (rating != null && rating > 0) {
            val dcgForHit = rating / ln((position + 1).toDouble())
            hits.add(documentReference to dcgForHit)
            dcg += dcgForHit
        }
        position++
    }
    return dcg
}

suspend fun SearchPlugin.discountedCumulativeGain(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    useLinearGains: Boolean= false
): MetricResults {
    val dcgFunction = if(useLinearGains) ::linearDcg else ::dcgExponential
    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()

        val unRated = results.searchResultList.filter { ratedSearch.ratings[it.id] == null }
            .map { MetricResults.DocumentReference(it.id, it.label) }
        val hitsWithRating = results.searchResultList.map { result ->
            MetricResults.DocumentReference(result.id, result.label) to ratedSearch.ratings[result.id]?.rating
        }

        val dcg = dcgFunction(hitsWithRating, hits)

        MetricResults.MetricResult(
            id = ratedSearch.id, metric = dcg, hits = hits, unRated = unRated
        )
    }

    val averageDcg = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(averageDcg, metricResults)
}

suspend fun SearchPlugin.normalizedDiscountedCumulativeGain(
    ratedSearches: List<RatedSearch>,
    k: Int = 5,
    useLinearGains: Boolean= false
): MetricResults {
    val dcgFunction = if(useLinearGains) ::linearDcg else ::dcgExponential

    val searchResults = ratedSearches.map { it to fetch(it.searchContext, k).getOrThrow() }

    val metricResults = searchResults.map { (ratedSearch, results) ->
        val hits = mutableListOf<Pair<MetricResults.DocumentReference, Double>>()

        val unRated = results.searchResultList.filter { ratedSearch.ratings[it.id] == null }
            .map { MetricResults.DocumentReference(it.id, it.label) }
        val rs = results.searchResultList.map { result ->
            MetricResults.DocumentReference(result.id, result.label) to ratedSearch.ratings[result.id]?.rating
        }

        val dcg = dcgFunction(rs, hits)

        val allRatings =
            ratedSearch.ratings
                .map { MetricResults.DocumentReference(it.documentId, it.label) to it.rating }
                // best rated should be at the beginning
                .sortedByDescending { (_, r) -> r }

        // ideal dcg of the best rated things coming out on top
        val idcg = dcgFunction(
            allRatings.subList(0, min(allRatings.size, rs.size)),
            mutableListOf()
        )
        val normalized = if (idcg == 0.0) {
            0.0
        } else {
            dcg / idcg
        }
        MetricResults.MetricResult(
            id = ratedSearch.id, metric = normalized, hits = hits, unRated = unRated
        )
    }

    val averageDcg = metricResults.sumOf { it.metric } / metricResults.size
    return MetricResults(averageDcg, metricResults)
}