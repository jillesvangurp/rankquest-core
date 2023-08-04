package com.jillesvangurp.ktranker.moviesrankexample

import com.jilesvangurp.ktranker.DEFAULT_PRETTY_JSON
import com.jilesvangurp.ktranker.RatedSearch
import com.jilesvangurp.ktranker.precisionAtK
import com.jilesvangurp.ktranker.runAllMetrics
import com.jilesvangurp.ktranker.testutils.coRun
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import org.junit.Test

class MovieSearchTest {
    val moviesSearch = SimpleMoviesSearch()

    @Test
    fun shouldSearch() = coRun {

        val results = moviesSearch.fetch(mapOf("query" to "hasta"), 10)
//        println(DEFAULT_PRETTY_JSON.encodeToString(results))
        results.resultList.first().label shouldContain "Hasta la Vista, baby"

        val qs = listOf(
            RatedSearch(
                "1", mapOf("query" to "hasta"), mapOf(
                    "583296" to 2,
                    "1087086" to 1
                )
            ),
            RatedSearch(
                "1", mapOf("query" to "blabla"), mapOf(
                    "583296" to 2,
                    "1087086" to 1
                )
            ),
        )

        moviesSearch.runAllMetrics(qs).forEach { (m,results)->
            results.metric shouldBeGreaterThan 0.0
        }
    }


}