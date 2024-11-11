@file:OptIn(ExperimentalSerializationApi::class)

package com.jilesvangurp.rankquest.core.pluginconfiguration

import com.jilesvangurp.rankquest.core.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int

val Int.primitive get() = JsonPrimitive(this)
val Double.primitive get() = JsonPrimitive(this)
val Boolean.primitive get() = JsonPrimitive(this)
val String.primitive get() = JsonPrimitive(this)

@Serializable
enum class Metric(
    val supportedParams: List<MetricParam>,
    val defaultExpected: Double = 0.8,
) {
    PrecisionAtK(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("relevantRatingThreshold", 1.primitive),
        ),

        ),
    RecallAtK(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("relevantRatingThreshold", 1.primitive),
        ),
    ),
    MeanReciprocalRank(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("relevantRatingThreshold", 1.primitive),
        ),
    ),
    ExpectedReciprocalRank(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("maxRelevance", 5.primitive),
        ),
    ),
    DiscountedCumulativeGain(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("useLinearGains", false.primitive)
        ),
    ),
    NormalizedDiscountedCumulativeGain(
        supportedParams = listOf(
            MetricParam("k", 5.primitive),
            MetricParam("useLinearGains", false.primitive)
        ),
    ), ;

    internal fun getIntParamValue(name: String, params: List<MetricParam>): Int =
        (params.firstOrNull { it.name == name } ?: supportedParams.firstOrNull { it.name == name })?.value?.int
            ?: 1

    internal fun getBoolParamValue(name: String, params: List<MetricParam>): Boolean =
        (params.firstOrNull { it.name == name }
            ?: supportedParams.firstOrNull { it.name == name })?.value?.boolean ?: true

    suspend fun run(searchPlugin: SearchPlugin, ratedSearches: List<RatedSearch>, params: List<MetricParam>, chunkSize: Int = 4) =
        when (this) {
            PrecisionAtK -> searchPlugin.precisionAtK(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = getIntParamValue("relevantRatingThreshold", params),
                k = getIntParamValue("k", params),
                chunkSize = chunkSize
            )

            RecallAtK -> searchPlugin.recallAtK(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = getIntParamValue("relevantRatingThreshold", params),
                k = getIntParamValue("k", params),
                chunkSize = chunkSize
            )

            MeanReciprocalRank -> searchPlugin.meanReciprocalRank(
                ratedSearches = ratedSearches,
                relevantRatingThreshold = getIntParamValue("relevantRatingThreshold", params),
                k = getIntParamValue("k", params),
                chunkSize = chunkSize
            )

            ExpectedReciprocalRank -> searchPlugin.expectedReciprocalRank(
                ratedSearches = ratedSearches,
                maxRelevance = getIntParamValue("maxRelevance", params),
                k = getIntParamValue("k", params),
                chunkSize = chunkSize
            )

            DiscountedCumulativeGain -> searchPlugin.discountedCumulativeGain(
                ratedSearches = ratedSearches,
                k = getIntParamValue("k", params),
                useLinearGains = getBoolParamValue("useLinearGains", params),
                chunkSize = chunkSize
            )

            NormalizedDiscountedCumulativeGain -> searchPlugin.normalizedDiscountedCumulativeGain(
                ratedSearches = ratedSearches,
                k = getIntParamValue("k", params),
                useLinearGains = getBoolParamValue("useLinearGains", params),
                chunkSize = chunkSize
            )
        }
}

@Serializable
data class MetricParam(val name: String, val value: JsonPrimitive)

@Serializable
data class MetricConfiguration(
    val name: String,
    val metric: Metric,
    val params: List<MetricParam>,
    @EncodeDefault val expected: Double? = null
)

@Serializable
sealed interface SearchContextField {
    val name: String
    val help: String
    val required: Boolean

    @Serializable
    @SerialName("str")
    data class StringField(
        override val name: String,
        override val help: String = "",
        override val required: Boolean = false,
        @EncodeDefault val defaultValue: String? = null,
        @EncodeDefault val placeHolder: String = "",
    ) : SearchContextField

    @Serializable
    @SerialName("int")
    data class IntField(
        override val name: String,
        override val help: String = "",
        override val required: Boolean = false,
        @EncodeDefault val defaultValue: Int?=null,
        @EncodeDefault val placeHolder: String = "0",
    ) : SearchContextField

    @Serializable
    @SerialName("double")
    data class DoubleField(
        override val name: String,
        override val help: String = "",
        override val required: Boolean = false,
        @EncodeDefault val defaultValue: Double?=null,
        @EncodeDefault val placeHolder: String = "0.0",
    ) : SearchContextField

    @Serializable
    @SerialName("bool")
    data class BoolField(
        override val name: String,
        override val help: String = "",
        override val required: Boolean = false,
        @EncodeDefault val defaultValue: Boolean? = null
    ) : SearchContextField
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
    val id: String,
    val name: String,
    val pluginType: String,
    val fieldConfig: List<SearchContextField>,
    val metrics: List<MetricConfiguration>,
    val pluginSettings: JsonObject? = null,
)

@Serializable
data class MetricsOutput(
    val searchConfigurationName: String, val configuration: MetricConfiguration, val results: MetricResults
)


