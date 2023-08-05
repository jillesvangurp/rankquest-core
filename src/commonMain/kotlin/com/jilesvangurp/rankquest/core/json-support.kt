package com.jilesvangurp.rankquest.core

import kotlinx.serialization.json.Json

val DEFAULT_JSON: Json = Json {
    // don't rely on external systems being written in kotlin or even having a language with default values
    // the default of false is FFing insane and dangerous
    encodeDefaults = true
    // save space
    prettyPrint = false
    // people adding shit to the json is OK, we're forward compatible and will just ignore it
    isLenient = true
    // encoding nulls is meaningless and a waste of space.
    explicitNulls = false
    // adding new fields OK even if older clients won't understand it
    ignoreUnknownKeys=true
    // ignore unknown enum values
    coerceInputValues=true
    // handle NaN and infinity
    allowSpecialFloatingPointValues=true
}

val DEFAULT_PRETTY_JSON: Json = Json {
    encodeDefaults = true
    prettyPrint = true
    isLenient = true
    explicitNulls = false
    ignoreUnknownKeys=true
    coerceInputValues=true
    allowSpecialFloatingPointValues=true
}
