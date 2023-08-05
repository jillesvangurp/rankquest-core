package com.jilesvangurp.rankquest.core

import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration

fun List<List<Int>>.ratings() = indices.map { queryIndex ->
    val ratingsForQuery = get(queryIndex)
    ratingsForQuery.indices.map { ratingIndex ->
        val r = ratingsForQuery[ratingIndex]
        SearchResultRating("${ratingIndex + 1}", r)
    }.let { searchResultRatings ->
        RatedSearch(
            id = "${queryIndex + 1}",
            searchContext = mapOf("query" to "test"),
            ratings = searchResultRatings
        )
    }
}

fun mock(ids: List<Int> = listOf(1, 2)) = object : SearchPlugin {
    override suspend fun fetch(
        searchContext: Map<String, String>,
        numberOfItemsToFetch: Int
    ): Result<SearchResults> =
        ids.map { SearchResults.SearchResult(it.toString()) }.let { results ->
            Result.success(SearchResults(results.size.toLong(), Duration.ZERO, results))
        }
}

class MetricsTest {

    @Test
    fun shouldCalculateMeanReciprocal() = coRun {
        val ratedSearches = listOf(
            listOf(1, 2),
            listOf(2, 1)
        ).ratings()


        val result =
            mock().meanReciprocalRank(ratedSearches, k = 2)


        result.metric shouldBe (1.0 + 0.5 + 0.5 + 1.0) / 2
    }

    @Test
    fun shouldCalculatePrecisionAt() = coRun {

        val ratedSearches = listOf(
            listOf(1, 0),
            listOf(0, 1)
        ).ratings()

        val result =
            mock().precisionAtK(ratedSearches, k = 2)


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

        result.metric shouldBe 0.5
    }

    @Test
    fun calculateRecallAtK() = coRun {

        val ratedSearches = listOf(
            listOf(1, 0),
            listOf(0, 1)
        ).ratings()

        val result =
            mock().recallAtK(ratedSearches, k = 2)


        result.metric shouldBe 1.0
    }
}
