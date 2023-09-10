package com.jillesvangurp.rankquest.core.moviesrankexample

import com.jilesvangurp.rankquest.core.RatedSearch
import com.jilesvangurp.rankquest.core.SearchResultRating
import com.jilesvangurp.rankquest.core.pluginconfiguration.Metric
import com.jilesvangurp.rankquest.core.pluginconfiguration.MetricConfiguration
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchContextField
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import com.jilesvangurp.rankquest.core.plugins.PluginFactoryRegistry
import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.string.shouldContain
import org.junit.Test

class MovieSearchTest {
    val pluginRegistry = PluginFactoryRegistry().let {
        it.register("movies", SimpleMoviesSearchFactory())
        it
    }

    val moviesSearchConfig = SearchPluginConfiguration(
        id="movies",
        "My Movies Search",
        "movies",
        fieldConfig = listOf(SearchContextField.StringField("query")),
        pluginSettings = null,
        metrics = Metric.entries.map { MetricConfiguration(it.name, it, listOf()) }
    )


    @Test
    fun shouldSearch() = coRun {
        val moviesSearch = pluginRegistry["movies"]!!.create(moviesSearchConfig)

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

        moviesSearchConfig.metrics.forEach { m ->
            val result = m.metric.run(moviesSearch,qs,m.params)
            result.metric shouldBeGreaterThan 0.0
        }
    }
}