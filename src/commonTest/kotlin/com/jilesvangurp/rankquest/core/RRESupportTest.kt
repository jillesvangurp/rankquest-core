package com.jilesvangurp.rankquest.core

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.encodeToString
import kotlin.test.Test

class RRESupportTest {

    @Test
    fun shouldParseRREFormat() {
        val rre = DEFAULT_JSON.decodeFromString(RRE.serializer(), sampleRre)
        println(DEFAULT_PRETTY_JSON.encodeToString(rre))

        val ratings = rre.toRatings()
        println(DEFAULT_PRETTY_JSON.encodeToString(ratings))

        ratings shouldHaveSize 5
        // ids should be unique
        ratings.map { it.id }.toSet() shouldHaveSize 5
        assertSoftly {
            ratings.forEach {
                withClue(
                    it
                ) {
                    // should strip $
                    it.searchContext.keys.joinToString(",") shouldNotContain "${'$'}"
                }
                // there should be some ratings
                it.ratings shouldHaveAtLeastSize 1
            }
        }
    }
}

val sampleRre = """
{
  "index": "core1",
  "corpora_file": "electric_basses.json",
  "id_field": "id",
  "topics": [
    {
      "description": "Fender basses",
      "query_groups": [
        {
          "name": "Brand search",
          "description": "The group tests several searches on the Fender brand",
          "queries": [
            {
              "template": "only_q.json",
              "placeholders": {
                "${"$"}query": "fender"
              }
            },
            {
              "template": "only_q.json",
              "placeholders": {
                "${"$"}query": "fender Bass"
              }
            },
            {
              "template": "filter_by_number_of_strings.json",
              "placeholders": {
                "${"$"}query": "Fender",
                "${"$"}strings": 4
              }
            }
          ],
          "relevant_documents": {
            "1": {
              "gain": 3
            },
            "2": {
              "gain": 3
            }
          }
        },
        {
          "name": "Jazz bass search",
          "description": "Several searches on a given model (Jazz bass)",
          "queries": [
            {
              "template": "only_q.json",
              "placeholders": {
                "${"$"}query": "jazz"
              }
            },
            {
              "template": "only_q.json",
              "placeholders": {
                "${"$"}query": "Jazz bass"
              }
            }
          ],
          "relevant_documents": {
            "1": {
              "gain": 3
            }
          }
        }
      ]
    }
  ]
}
""".trimIndent()