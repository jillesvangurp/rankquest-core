package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class JsonPostAPIPluginKtTest {
    @Test
    fun shouldApplyTemplate() {
        val template="""
            {
                "f1":"{{ param1 }}",
                "f2":"{{param2 }}",
                "f3":"{{    param3}}",
                "f4":"{{param4}}"                
            }
        """.trimIndent()

        template.applySearchContext(mapOf(
            "param1" to "p1",
            "param2" to "p2",
            "param3" to "p3",
            "param4" to "p4",
        )).let { templated ->
            DEFAULT_JSON.decodeFromString<JsonObject>(templated).let { obj ->
                obj["f1"]?.jsonPrimitive?.content shouldBe "p1"
                obj["f2"]?.jsonPrimitive?.content shouldBe "p2"
                obj["f3"]?.jsonPrimitive?.content shouldBe "p3"
                obj["f4"]?.jsonPrimitive?.content shouldBe "p4"
            }
        }
    }
}