package com.jilesvangurp.ktranker

import kotlinx.serialization.Serializable

/**
 * Rating for a [documentId], optionally may include a human readable [label]
 * and a [comment] detailing the reasoning for the rating.
 */
@Serializable
data class SearchResultRating(val documentId: String, val rating: Int=0, val label: String?=null, val comment: String?=null)

/**
 * Find a rating by [documentId].
 */
operator fun List<SearchResultRating>.get(documentId: String) = firstOrNull { it.documentId == documentId }

