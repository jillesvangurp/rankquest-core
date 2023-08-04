package com.jilesvangurp.ktranker

import com.jilesvangurp.ktranker.testutils.coRun
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration


class SearchPluginKtTest {
    @Test
    fun shouldCalculateMeanReciprocal() = coRun {
        val mockPlugin = object : SearchPlugin {
            override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int) =
                Results(2, Duration.ZERO, listOf(Results.Result("1"), Results.Result("2")))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 1, "2" to 2)
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "icle"),
                ratings = mapOf("1" to 2, "2" to 1)
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
                Results(2, Duration.ZERO, listOf(Results.Result("1"), Results.Result("2")))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 1, "2" to 0)
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 0, "2" to 1)
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
                Results(2, Duration.ZERO, listOf(Results.Result("1"), Results.Result("2")))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 1, "2" to 0)
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 0, "2" to 1)
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
