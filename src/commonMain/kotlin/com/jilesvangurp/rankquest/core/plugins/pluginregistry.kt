package com.jilesvangurp.rankquest.core.plugins

import com.jilesvangurp.rankquest.core.SearchPlugin
import com.jilesvangurp.rankquest.core.pluginconfiguration.SearchPluginConfiguration


interface PluginFactory {
    fun create(configuration: SearchPluginConfiguration): SearchPlugin
}

enum class BuiltinPlugins {
    RestAPIPlugin,
    ElasticSearch, // TODO
}

class PluginFactoryRegistry {
    private val registry: MutableMap<String, PluginFactory> = mutableMapOf(
        BuiltinPlugins.RestAPIPlugin.name to RestAPIPluginFactory()
    )

    fun register(name:String, factory: PluginFactory) {
        if(registry.keys.contains(name)) {
            error("cannot overwrite existing factories")
        }
        registry[name] = factory
    }
}

