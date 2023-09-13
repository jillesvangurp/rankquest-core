package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.DEFAULT_PRETTY_JSON
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchContextField
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import com.jilesvangurp.rankquest.core.testutils.coRun
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.Test

@Serializable
data class SampleHit(val id: String, val label: String)

@Serializable
data class SampleResponse(val hits: List<SampleHit>)

private val serverPort = 8666

private val pluginFactoryRegistry = PluginFactoryRegistry(HttpClient(CIO) {
    Json {
        DEFAULT_JSON
    }
})

suspend fun withServer(block: suspend () -> Unit) {
    val server = embeddedServer(Netty, port = serverPort) {
        routing {
            get("/search_get") {
                call.respondText(
                    DEFAULT_JSON.encodeToString(
                        SampleResponse(
                            listOf(
                                SampleHit("1", "one"),
                                SampleHit("2", "two"),
                            )
                        )
                    ), contentType = ContentType.Application.Json
                )
            }

            post("/search_post") {
                call.receiveText()
                call.respondText(
                    DEFAULT_JSON.encodeToString(
                        SampleResponse(
                            listOf(
                                SampleHit("1", "one"),
                                SampleHit("2", "two"),
                            )
                        )
                    ), contentType = ContentType.Application.Json
                )
            }
        }
    }
    server.start()

    block.invoke()

    server.stop()
}

class JsonRestPluginsTest {
    @Test
    fun `should return json using get plugin and use it`() = coRun {
        withServer {
            val rp = JsonGetAPIPluginConfig(
                searchUrl = "http://localhost:$serverPort/search_get",
                requestHeaders = mapOf(),
                searchContextParams = mapOf(),
                jsonPathToHits = listOf("hits"),
                jsonPathToId = listOf("id"),
                jsonPathToLabel = listOf("label"),
            )
            
            val config = SearchPluginConfiguration(
                id = "get-it",
                name = "get-it",
                pluginType = BuiltinPlugins.JsonGetAPIPlugin.name,
                fieldConfig = listOf(),
                metrics = listOf(),
                pluginSettings = DEFAULT_JSON.encodeToJsonElement(rp).jsonObject
            )
            val plugin = pluginFactoryRegistry[config.pluginType]!!.create(config)

            val response = plugin.fetch(mapOf(), 5).getOrThrow()
            response.total shouldBe 2
        }
    }

    @Test
    fun `should return json using post plugin and use it`() = coRun {
        withServer {
            val rp = JsonPostAPIPluginConfig(
                searchUrl = "http://localhost:$serverPort/search_post",
                requestHeaders = mapOf(
                    "content-type" to "application/json"
                ),
                requestBodyTemplate = """
                    {
                        "text":"{{ q }}"
                    }
                """.trimIndent(),
                jsonPathToHits = listOf("hits"),
                jsonPathToId = listOf("id"),
                jsonPathToLabel = listOf("label"),
            )


            val config = SearchPluginConfiguration(
                id="post-it",
                name = "post-it",
                pluginType = BuiltinPlugins.JsonPostAPIPlugin.name,
                fieldConfig = listOf(
                    SearchContextField.StringField("q", placeHolder = "Type your query")
                ),
                metrics = listOf(
                ),
                pluginSettings = DEFAULT_JSON.encodeToJsonElement(rp).jsonObject
            )
            val plugin = pluginFactoryRegistry[config.pluginType]!!.create(config)

            val response = plugin.fetch(
                mapOf(
                    "q" to "ohai"
                ), 5
            ).getOrThrow()
            response.total shouldBe 2
        }
    }
}