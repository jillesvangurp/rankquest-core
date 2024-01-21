package com.jilesvangurp.rankquest.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RREGain(val gain: Int? = null, val rating: Int? = null)

@Serializable
data class RREQuery(
    val template: String,
    val placeholders: Map<String, String>
)

@Serializable
data class RREQueryGroup(
    val name: String?,
    val description: String?,
    val queries: List<RREQuery>,
    @SerialName("relevant_documents")
    val relevantDocuments: Map<String, RREGain>
)


@Serializable
data class RreTopic(
    val description: String?,
    @SerialName("query_groups")
    val queryGroups: List<RREQueryGroup>
)

@Serializable
data class RRE(
    val index: String?,
    @SerialName("corpora_file")
    val corporaFile: String?,
    @SerialName("id_field")
    val idField: String = "id",
    val topics: List<RreTopic>
)

/**
 * Opinionated conversion. Rre seems to allow combining queries for multiple templates. A ranquest
 * search configuration does not allow this. So, the conversion assumes that the intention is to
 * run all test cases with the same configuration.
 *
 * To facilitate postprocessing, the template is preserved a as a "_template" value in the search
 * context. You could group the ratings by template and create separate plugin configurations for each.
 */
fun RRE.toRatings(): List<RatedSearch> {
    var id = 0
    return topics.flatMap { topic ->
        topic.queryGroups.flatMap { group ->
            val ratings = group.relevantDocuments.mapNotNull { (id, gain) ->
                val rating = gain.gain ?: gain.rating
                if (rating != null) {
                    SearchResultRating(documentId = id, rating = rating)
                } else null
            }
            group.queries.map { query ->
                RatedSearch(
                    id = (id++).toString(),
                    ratings = ratings,
                    comment = group.description,
                    tags = listOfNotNull(
                        group.name, query.template
                    ),
                    searchContext = query.placeholders
                        .map { (k, v) ->
                            k.replace("${'$'}", "") to v
                        }.toMap().let {
                            // add _template variable to context
                            it + ("_template" to query.template)
                        },
                )
            }
        }
    }
}

fun List<RatedSearch>.toRRE(index: String = "ranquest", corporaFile: String = "ranquest", idField: String = "id"): RRE {

    val groups = groupBy {
        it.tags
    }

    val topics = groups.map { (ts, rs) ->
        // treat each group of tags as a topic
        RreTopic(
            description = ts?.joinToString(",") ?: "Rankquest Rated Searches",
            queryGroups = rs.map { ratedSearch ->
                // one query group per rated search
                RREQueryGroup(
                    name = ratedSearch.id,
                    description = ratedSearch.comment,
                    queries = listOf(
                        RREQuery(ratedSearch.id, ratedSearch.searchContext.map { (k, v) ->
                            // rre use a $ prefix
                            "${'$'}$k" to v
                        }.toMap())
                    ),
                    relevantDocuments = ratedSearch.ratings.map {
                        it.documentId to RREGain(gain = it.rating)
                    }.toMap()
                )
            }
        )
    }
    return RRE(index = index, corporaFile = corporaFile, idField = idField, topics = topics)
}