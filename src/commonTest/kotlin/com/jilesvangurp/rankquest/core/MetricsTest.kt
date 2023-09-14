package com.jilesvangurp.rankquest.core

import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.time.Duration

fun List<List<Int>>.ratings() = indices.map { queryIndex ->
    val ratingsForQuery = get(queryIndex)
    ratingsForQuery.indices.map { ratingIndex ->
        val r = ratingsForQuery[ratingIndex]
        SearchResultRating("${ratingIndex + 1}", r)
    }.let { searchResultRatings ->
        RatedSearch(
            id = "${queryIndex + 1}", searchContext = mapOf("query" to "test"), ratings = searchResultRatings
        )
    }
}

fun mock(ids: List<Int> = listOf(1, 2)) = object : SearchPlugin {
    override suspend fun fetch(
        searchContext: Map<String, String>, numberOfItemsToFetch: Int
    ): Result<SearchResults> = ids.map { SearchResults.SearchResult(it.toString()) }.let { results ->
        Result.success(SearchResults(results.size.toLong(), Duration.ZERO, results))
    }
}

class MetricsTest {

    @Test
    fun shouldCalculateMeanReciprocal() = coRun {
        mock(listOf(1, 2)).meanReciprocalRank(
            listOf(
                listOf(1, 2), listOf(2, 1)
            ).ratings(), k = 2, relevantRatingThreshold = 2
        ).metric shouldBe (0.5 + 1) / 2.0
        mock(listOf(2, 1)).meanReciprocalRank(
            listOf(
                listOf(1, 2), listOf(2, 1)
            ).ratings(), k = 2, relevantRatingThreshold = 1
        ).metric shouldBe (1 + 1) /2.0
    }

    @Test
    fun shouldCalculatePrecisionAt() = coRun {

        mock().precisionAtK(
            ratedSearches = listOf(
                listOf(1, 0), listOf(0, 1)
            ).ratings(),
            k = 2
        ).metric shouldBe 1.0
        mock().precisionAtK(
            ratedSearches = listOf(
                listOf(1, 1, 2, 2), listOf(2, 2, 1, 1)
            ).ratings(),
            k = 2,
            relevantRatingThreshold = 2
        ).metric shouldBe 0.5
    }

    @Test
    fun calculateRecallAtK() = coRun {

        val ratedSearches = listOf(
            listOf(1, 0), listOf(0, 1)
        ).ratings()

        val result = mock().recallAtK(ratedSearches, k = 2)

        result.metric shouldBe 1.0
    }

    @Test
    fun calculateERR() = coRun {
        mock().expectedReciprocalRank(
            listOf(
                listOf(5, 5), listOf(5, 5)
            ).ratings(), maxRelevance = 5
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 0.95
            result.metric shouldBeLessThan 1.0
            result.details shouldHaveAtLeastSize 1
            result.details.forEach {mr ->
                mr.hits shouldHaveAtLeastSize 1
            }
        }
        // nothing relevant
        mock().expectedReciprocalRank(
            listOf(
                listOf(0, 0), listOf(0, 0)
            ).ratings(), maxRelevance = 5
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBe 0.0
        }
        mock().expectedReciprocalRank(
            listOf(
                listOf(0, 1), listOf(0, 0)
            ).ratings(), maxRelevance = 5
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 0.0
            result.metric shouldBeLessThan 0.1
        }
    }

    @Test
    fun shouldCalculateNDcg() = coRun {
        mock().normalizedDiscountedCumulativeGain(
            listOf(
                listOf(2, 2), listOf(1, 1)
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBe 1.0
            result.details shouldHaveAtLeastSize 1
            result.details.forEach {mr ->
                mr.hits shouldHaveAtLeastSize 1
            }
        }

        mock().normalizedDiscountedCumulativeGain(
            listOf(
                listOf(2, 2), // 1.0
                listOf(0, 0) // 0.0
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBe 0.5
        }


        mock().normalizedDiscountedCumulativeGain(
            listOf(
                listOf(0, 5), listOf(0, 1000)
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 0.6
            result.metric shouldBeLessThan 0.7
        }
    }

    @Test
    fun shouldCalculateNdcgBetween_0_and_1() = coRun {
        // these results have
        mock(listOf(1,2,3,4,5)).normalizedDiscountedCumulativeGain(
            listOf(
                listOf(3, 2), listOf(5, 1)
            ).ratings()
        ).let { result ->
            result.metric shouldBe   1.0
        }
        mock(listOf(1,2,3,4,5).reversed()).normalizedDiscountedCumulativeGain(
            listOf(
                listOf(3, 2), listOf(5, 1)
            ).ratings()
        ).let { result ->
            result.metric shouldBeGreaterThanOrEqual  0.0
            result.metric shouldBeLessThanOrEqual  1.0
        }
    }

    @Test
    fun shouldCalculateDcg() = coRun {
        mock().discountedCumulativeGain(
            listOf(
                listOf(2, 2), listOf(1, 1)
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 3.0
            result.metric shouldBeLessThan 4.0
        }

        mock().discountedCumulativeGain(
            listOf(
                listOf(2, 2), // 1.0
                listOf(0, 0) // 0.0
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 2.0
            result.metric shouldBeLessThan 3.0
        }


        mock().discountedCumulativeGain(
            listOf(
                listOf(0, 5), listOf(0, 1)
            ).ratings()
        ).let { result ->
            println(DEFAULT_PRETTY_JSON.encodeToJsonElement(result))
            result.metric shouldBeGreaterThan 10.0
            result.metric shouldBeLessThan 11.0
        }
    }
}
