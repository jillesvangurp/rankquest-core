package com.jilesvangurp.rankquest.core

import kotlinx.serialization.Serializable

/**
 * A rated search has an id, a search context with all the information
 * your [SearchPlugin] needs, and a list of [SearchResultRating].
 *
 * The interpretation of the rating is different per metric. But a non-zero
 * rating generally indicates some relevance, whereas a zero rating
 * means the result is not relevant to the search.
 */
@Serializable
data class RatedSearch(
    val id: String,
    val searchContext: Map<String, String>,
    val ratings: List<SearchResultRating>,
    val comment: String?=null,
)
