package com.jillesvangurp.rankquest.core.moviesrankexample

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.SearchResults
import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import com.jilesvangurp.rankquest.core.plugins.PluginFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import search.*
import kotlin.math.min
import kotlin.time.measureTimedValue

@Serializable
data class Movie(
    val quote: String,
    val movie: String,
    val year: Int,
    val type: String,
)

val Movie.id get() = (quote.length *movie.length * year).toString() // probably unique within this dataset
val Movie.label get() = "${quote.substring(0..<min(quote.length,40))}... - $movie ($year)"
fun Movie.toDoc() = Document(
    id = id,
    mapOf(
        "quote" to listOf(quote),
        "movie" to listOf(movie),
    )
)
fun loadMovies(): List<Movie> {
    val json = Movie::class.java.classLoader.getResource("movie_quotes.json")?.readText() ?: error("resource not found")

    return DEFAULT_JSON.decodeFromString(ListSerializer(Movie.serializer()), json)
}

val movies = loadMovies().associateBy { it.id }

val index by lazy {
    DocumentIndex(
        mutableMapOf(
            "quote" to TextFieldIndex(),
            "movie" to TextFieldIndex()
        )
    ).also { idx ->
        movies.values.forEach {
            idx.index(it.toDoc())
        }
    }
}

class SimpleMoviesSearch : SearchPlugin {
    override suspend fun fetch(searchContext: Map<String, String>, numberOfItemsToFetch: Int): Result<SearchResults> {
        return Result.success(measureTimedValue {
            // uses my ktjsearch in memory search engine
            index.search {
                query = MatchQuery("quote", searchContext["query"] ?: error("search context must have query"))
            }
        }.let { (hits,duration)->
            SearchResults(hits.size.toLong(),duration,hits.map { SearchResults.SearchResult(it.first, movies[it.first]?.label?:error("")) })
        })
    }
}

class SimpleMoviesSearchFactory() : PluginFactory {
    override fun create(configuration: SearchPluginConfiguration): SearchPlugin {
        return SimpleMoviesSearch()
    }

}

