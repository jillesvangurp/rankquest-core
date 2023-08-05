package com.jillesvangurp.rankquest.core.moviesrankexample

import com.jilesvangurp.rankquest.core.RatedSearch
import com.jilesvangurp.rankquest.core.SearchResultRating
import com.jilesvangurp.rankquest.core.runAllMetrics
import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.string.shouldContain
import org.junit.Test

class MovieSearchTest {
    val moviesSearch = SimpleMoviesSearch()

    @Test
    fun shouldSearch() = coRun {
        val results = moviesSearch.fetch(mapOf("query" to "hasta"), 10).getOrThrow()
//        println(DEFAULT_PRETTY_JSON.encodeToString(results))
        results.searchResultList.first().label shouldContain "Hasta la Vista, baby"

        val qs = listOf(
            RatedSearch(
                "1", mapOf("query" to "hasta"), listOf(
                    SearchResultRating("583296", 2),
                    SearchResultRating("1087086", 1)
                )
            ),
            RatedSearch(
                "1", mapOf("query" to "blabla"), listOf(
                    SearchResultRating("583296", 2),
                    SearchResultRating("1087086", 1)
                )
            ),
        )

        moviesSearch.runAllMetrics(qs).forEach { (m, results) ->
            results.metric shouldBeGreaterThan 0.0
        }
    }
}