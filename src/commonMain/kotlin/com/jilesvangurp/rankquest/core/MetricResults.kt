package com.jilesvangurp.rankquest.core

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The main [metric] is provided at the top level. The [details] contain
 * the results for each [RatedSearch].
 */
@Serializable
data class MetricResults(
    val metric: Double,
    val details: List<MetricResult>
) {
    /**
     * A metric result for a given [RatedSearch]. The [id] refers to the [RatedSearch], the
     * [hits] mao the document id to the calculated metric of that document. Finally,
     * a list of [unRated] document ids is also provided.
      */
    @Serializable
    data class MetricResult(
        val id: String,
        val metric: Double,
        val hits: List<Pair<DocumentReference,Double>>,
        val unRated: List<DocumentReference>
    )

    @Serializable
    data class DocumentReference(val docId: String, val label:String?)
}

@Serializable
data class Stats(val min: Double, val max: Double, val mean: Double,val median: Double, val variance: Double,val standardDeviation: Double)

val MetricResults.resultScores get() = details.flatMap {
    it.hits.map {(_,score)->
        score
    }
}

val MetricResults.scores get() = details.map { it.metric }

val List<Double>.stats: Stats get() {
    val mean = sum() / size
    val median = if(size>0) {
        get(size/2)
    } else 0.0

    val variance = map { (it - mean).pow(2) }.sum() / size
    return Stats(
        min = min(),
        max = max(),
        mean = mean,
        median = median,
        variance = variance,
        standardDeviation = sqrt(variance)
    )
}