package com.jilesvangurp.ktranker

import kotlinx.serialization.Serializable

/**
 * Rating for a [documentId], optionally may include a human readable [label]
 */
@Serializable
data class SearchResultRating(val documentId: String, val rating: Int=0, val label: String?=null)

operator fun List<SearchResultRating>.get(documentId: String) = firstOrNull { it.documentId == documentId }

