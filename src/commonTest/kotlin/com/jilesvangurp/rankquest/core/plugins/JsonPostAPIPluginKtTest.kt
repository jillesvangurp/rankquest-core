package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import io.kotest.matchers.sequences.shouldHaveSize
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


    @Test
    fun shouldListVariables() {
        val template="""
            {
                "f1":"{{ param1 }}",
                "f2":"{{param2 }}",
                "f3":"{{    param3}}",
                "f4":"{{param4}}"                
            }
        """.trimIndent()
        val re = "\\{\\{\\s*(.*?)\\s*\\}\\}".toRegex(RegexOption.MULTILINE)
        re.findAll(template).let {
            it.shouldHaveSize(4)
        }
    }

    @Test
    fun shouldBeValidJsonAfterReplace() {
        val template = """
            {
            	"from": 0,
            	"size": 5,
            	"groupIds": ["jRsP7_j9X1DRoAGr-I3GDg"],
            	"userId": "v6x2stTEE5njhCn1gSYVsA",
            	"text": "{{ text }}",
            	"objectTypes": [],
            	"orTags": [],
            	"excludeTags": [],
            	"centroid": {
            		"lat": 52.506907788863074,
            		"lon": 13.435339336434751
            	},
            	"includeDeleted": true
            }
        """.trimIndent()

        template.applySearchContext(mapOf("text" to "OHAI")).let {
            val o = DEFAULT_JSON.decodeFromString<JsonObject>(it)
            println(o)
        }

    }
}