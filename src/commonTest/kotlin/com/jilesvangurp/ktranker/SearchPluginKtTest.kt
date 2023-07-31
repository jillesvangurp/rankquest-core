package com.jilesvangurp.ktranker

import com.jilesvangurp.ktranker.testutils.coRun
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration


class SearchPluginKtTest {
    @Test
    fun shouldCalculateMeanReciprocal() = coRun {
        val mockPlugin = object : SearchPlugin {
            override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch:Int) =
                Results(2, Duration.ZERO, listOf(Result("1"), Result("2")))
        }

        val ratedSearches = listOf(
            RatedSearch(
                id = "1",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 1, "2" to 2)
            ),
            RatedSearch(
                id = "2",
                searchContext = mapOf("query" to "test"),
                ratings = mapOf("1" to 2, "2" to 1)
            )
        )

        val result =
            mockPlugin.meanReciprocalRank(ratedSearches, numberOfItemsToFetch = 2)


        val expectedResults = MetricResults(
            metric = (1.0 + 0.5 + 0.5 + 1.0) / 2,
            details = listOf(
                MetricResult(
                    id = "1",
                    metric = 1.5,
                    hits = listOf("1" to 1.0, "2" to 0.5),
                    unRated = emptyList()
                ),
                MetricResult(
                    id = "2",
                    metric = 1.5,
                    hits = listOf("1" to 0.5, "2" to 1.0),
                    unRated = emptyList()
                )
            )
        )

        result shouldBe expectedResults
    }
}
