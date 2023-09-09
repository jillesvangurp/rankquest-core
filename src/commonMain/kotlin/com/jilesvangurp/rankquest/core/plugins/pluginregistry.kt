package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.DEFAULT_JSON
import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration
import io.ktor.client.*
import kotlinx.serialization.json.Json


interface PluginFactory {
    fun create(configuration: SearchPluginConfiguration): SearchPlugin
}

enum class BuiltinPlugins {
    JsonGetAPIPlugin,
    JsonPostAPIPlugin,
    ElasticSearch, // TODO
}

class PluginFactoryRegistry(httpClient: HttpClient= HttpClient { Json { DEFAULT_JSON } }) {
    private val registry: MutableMap<String, PluginFactory> = mutableMapOf(
        BuiltinPlugins.JsonGetAPIPlugin.name to JsonGetAPIPluginFactory(httpClient=httpClient),
        BuiltinPlugins.JsonPostAPIPlugin.name to JsonPostAPIPluginFactory(httpClient=httpClient),
    )

    fun register(name:String, factory: PluginFactory) {
        if(registry.keys.contains(name)) {
            error("cannot overwrite existing factories")
        }
        registry[name] = factory
    }

    operator fun get(name: String): PluginFactory? {
        return  registry[name]
    }
}

