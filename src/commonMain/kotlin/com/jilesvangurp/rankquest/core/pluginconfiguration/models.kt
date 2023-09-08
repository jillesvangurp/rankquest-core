package com.jilesvangurp.rankquest.core.pluginconfiguration

import com.jilesvangurp.rankquest.core.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
enum class Metric(
    val supportedParams: List<MetricParam>
) {
    PrecisionAtK(
        supportedParams = listOf(
            MetricParam("k", 5),
            MetricParam("relevantRatingThreshold", 1),
        ),

        ),
    RecallAtK(
        supportedParams = listOf(
            MetricParam("k", 5),
            MetricParam("relevantRatingThreshold", 1),
        ),
    ),
    MeanReciprocalRank(
        supportedParams = listOf(
            MetricParam("k", 5),
            MetricParam("relevantRatingThreshold", 1),
        ),
    ),
    ExpectedReciprocalRank(
        supportedParams = listOf(
            MetricParam("maxRelevance", 5),
        ),
    ),
    DiscountedCumulativeGain(
        supportedParams = listOf(
            MetricParam("k", 5),
        ),
    ),
    NormalizedDiscountedCumulativeGain(
        supportedParams = listOf(
            MetricParam("k", 5),

            ),
    ), ;

    internal fun getIntParamValue(name: String, params: List<MetricParam>): Int =
        (params.firstOrNull { it.name == name } ?: PrecisionAtK.supportedParams.firstOrNull { it.name == name })?.let {
            it.value
        } ?: 1

    suspend fun run(searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>) =
        implementation.evaluate(searchPlugin, ratedSearches, params)
}

internal val Metric.implementation
    get() = when (this) {
        Metric.PrecisionAtK -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.precisionAtK(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = Metric.PrecisionAtK.getIntParamValue("relevantRatingThreshold", params),
                k = Metric.PrecisionAtK.getIntParamValue("k", params)
            )
        }

        Metric.RecallAtK -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.recallAtK(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = Metric.PrecisionAtK.getIntParamValue("relevantRatingThreshold", params),
                k = Metric.PrecisionAtK.getIntParamValue("k", params)
            )
        }

        Metric.MeanReciprocalRank -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.meanReciprocalRank(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = Metric.PrecisionAtK.getIntParamValue("relevantRatingThreshold", params),
                k = Metric.PrecisionAtK.getIntParamValue("k", params)
            )
        }

        Metric.ExpectedReciprocalRank -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.expectedMeanReciprocalRank(
                ratedSearches = ratedSearches,
                maxRelevance = Metric.PrecisionAtK.getIntParamValue("maxRelevance", params),
            )
        }

        Metric.DiscountedCumulativeGain -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.discountedCumulativeGain(
                ratedSearches = ratedSearches,
                k = Metric.PrecisionAtK.getIntParamValue("k", params),
            )
        }

        Metric.NormalizedDiscountedCumulativeGain -> object : MetricImplementation {
            override suspend fun evaluate(
                searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>
            ): MetricResults = searchPlugin.normalizedDiscountedCumulativeGain(
                ratedSearches = ratedSearches,
                k = Metric.NormalizedDiscountedCumulativeGain.getIntParamValue("k", params)
            )
        }
    }

@Serializable
data class MetricParam(val name: String, val value: Int)

@Serializable
data class MetricConfiguration(val name: String, val metric: Metric, val params: List<MetricParam>)

@Serializable
sealed interface SearchContextField {
    val name: String

    @Serializable
    @SerialName("str")
    data class StringField(
        override val name: String,
        @EncodeDefault val placeHolder: String = "enter a query"
    ): SearchContextField

    @Serializable
    @SerialName("int")
    data class IntField(
        override val name: String,
        val defaultValue: Int = 0
    ): SearchContextField

    @Serializable
    @SerialName("bool")
    data class BoolField(
        override val name: String,
        val defaultValue: Boolean = false
    ): SearchContextField
}

/**
 * A plugin configuration is a complete description of how to use a given search service
 * to calculate metrics.
 *
 * It must include a [name] and a [pluginType] which is used to lookup and configure the correct implementation.
 *
 * [fieldConfig] defines a list of search context fields that are needed to complete a search (this is what goes in rated searches).
 * These are the parameters that your search service or API expects
 *
 * Any [pluginSettings] go in a JsonObject that may be used to configure the plugin further.
 * We use a JsonObject here so we can serialize and deserialize this along with the rest of the settings.
 *
 * Finally, you need to specify a list of [metrics] that you want to run.
 */
@Serializable
data class SearchPluginConfiguration(
    val name: String,
    val pluginType: String,
    val fieldConfig: List<SearchContextField>,
    val metrics: List<MetricConfiguration>,
    val pluginSettings: JsonObject?=null,
)

@Serializable
data class MetricsOutput(val searchConfigurationName: String,val configuration: MetricConfiguration, val results: MetricResults)


