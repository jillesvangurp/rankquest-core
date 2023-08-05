package com.jilesvangurp.rankquest.core

import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration


class SearchPluginTest {
    @Test
    fun shouldCalculateMeanReciprocal() = coRun {
        val mockPlugin = object : SearchPlugin {
            override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int) =
                Result.success(SearchResults(2, Duration.ZERO, listOf(SearchResults.SearchResult("1"), SearchResults.SearchResult("2"))))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = listOf(
                    SearchResultRating("1", 1),
                    SearchResultRating("2", 2)
                )
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "icle"),
                ratings = listOf(
                    SearchResultRating("1", 2),
                    SearchResultRating("2", 1)
                )
            )
        )

        val result =
            mockPlugin.meanReciprocalRank(ratedSearches, k = 2)


        val expectedResults = MetricResults(
            metric = (1.0 + 0.5 + 0.5 + 1.0) / 2,
            details = listOf(
                MetricResults.MetricResult(
                    id = "1",
                    metric = 1.5,
                    hits = listOf("1" to 1.0, "2" to 0.5),
                    unRated = emptyList()
                ),
                MetricResults.MetricResult(
                    id = "2",
                    metric = 1.5,
                    hits = listOf("1" to 0.5, "2" to 1.0),
                    unRated = emptyList()
                )
            )
        )

        result shouldBe expectedResults
    }

    @Test
    fun shouldCalculatePrecisionAt() = coRun {
        val mockPlugin = object : SearchPlugin {
            override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int) =
                Result.success(SearchResults(2, Duration.ZERO, listOf(SearchResults.SearchResult("1"), SearchResults.SearchResult("2"))))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings =listOf(
                    SearchResultRating("1", 1),
                    SearchResultRating("2", 0)
                )

            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "test"),
                ratings = listOf(
                    SearchResultRating("1", 0),
                    SearchResultRating("2", 1)
                )
            )
        )

        val result =
            mockPlugin.precisionAtK(ratedSearches, k = 2)


        val expectedResults = MetricResults(
            metric = 0.5,
            details = listOf(
                MetricResults.MetricResult(
                    id = "1",
                    metric = 0.5,
                    hits = listOf("1" to 1.0),
                    unRated = listOf("2")
                ),
                MetricResults.MetricResult(
                    id = "2",
                    metric = 0.5,
                    hits = listOf("2" to 1.0),
                    unRated = listOf("1")
                )
            )
        )

        result shouldBe expectedResults
    }

    @Test
    fun calculateRecallAtK() = coRun {
        val mockPlugin = object : SearchPlugin {
            override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int) =
                Result.success(SearchResults(2, Duration.ZERO, listOf(SearchResults.SearchResult("1"), SearchResults.SearchResult("2"))))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = listOf(
                    SearchResultRating("1", 1),
                    SearchResultRating("2", 0)
                )
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "test"),
                ratings = listOf(
                    SearchResultRating("1", 0),
                    SearchResultRating("2", 1)
                )
            )
        )

        val result =
            mockPlugin.recallAtK(ratedSearches, k = 2)


        val expectedResults = MetricResults(
            metric = 1.0,
            details = listOf(
                MetricResults.MetricResult(
                    id = "1",
                    metric = 1.0, // "1" is the only relevant document and it's retrieved
                    hits = listOf("1" to 1.0),
                    unRated = listOf("2")
                ),
                MetricResults.MetricResult(
                    id = "2",
                    metric = 1.0, // "2" is the only relevant document and it's retrieved
                    hits = listOf("2" to 1.0),
                    unRated = listOf("1")
                )
            )
        )

        result shouldBe expectedResults
    }
}