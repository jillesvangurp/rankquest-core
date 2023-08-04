package com.jilesvangurp.ktranker

import kotlinx.serialization.Serializable

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
        val hits: List<Pair<String, Double>>,
        val unRated: List<String>
    )
}