package com.jilesvangurp.rankquest.core.plugins

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonObject.getString(path: List<String>) = get(path)?.let {
    if (it is JsonPrimitive) it.content else it.toString()
}

fun JsonObject.get(path: List<String>): JsonElement? {
    var jsonElement: JsonElement? = this
    for (e in path) {
        if (jsonElement is JsonObject) {
            jsonElement = jsonElement.get(e)
        } else {
            jsonElement = null
            break
        }
    }

    return jsonElement
}