[![CI Build](https://github.com/jillesvangurp/rankquest-core/actions/workflows/pr_master.yaml/badge.svg)](https://github.com/jillesvangurp/rankquest-core/actions/workflows/pr_master.yaml)

Rankquest is a kotlin multiplatform rank evalution library that can be used to benchmark any API that returns search results.

## Rankquest Studio

Rankquest powers [Rankquest Studio](https://rankquest.jillesvangurp.com). This is a nice web UI for creating and
evaluating test cases that uses rankquest core. Using it require no installation. Everything happens in the browser and your configuration and test cases are stored locally in the browser. Like rankquest-core, Rankquest Studio is open source and you can find the source code [here](https://github.com/jillesvangurp/rankquest-studio).

## Rankquest Cli

Once you have created test cases with Rankquest Studio, you can run them on the command line with [rankquest-cli](https://github.com/jillesvangurp/rankquest-cli). You can easily integrate that into your builds as well.

## Features

This library provides portable implementations of common search rank evaluation algorithms. Currently it supports:

- PrecisionAtK
- RecallAtK
- MeanReciprocalRank
- ExpectedReciprocalRank
- DiscountedCumulativeGain
- NormalizedDiscountedCumulativeGain

Additionally it provides a lot of data classes that you can use to work with test cases, plugin configurations, and metric output. These data classes are of course Json serializable and are what Rankquest Studio uses for importing and exporting these as well.

## Why?

The rank eval API in Elasticsearch is nice but it is too low level. Some People use Elasticsearch
to implement search solutions. But of course there are many alternatives. Also, when testing and benchmarking search solutions, you want to do this in black box style and test APIs from the outside instead of benchmarking a specific query in Elasticsearch.

Rankquest addresses this by being implementation neutral. It works with anything that returns some kind of ordered list of results.

Making this a Kotlin multi platform library enables a few nice features, including embedding this in a kotlin-js web application where it can run entirely in the browser. This is what [rankquest-studio](https://rankquest.jillesvangurp.com) does. There is also [rankquest-cli](https://github.com/jillesvangurp/rankquest-cli), a command line tool that embeds rankquest-core that allows you to run your exported ratings from the commandline.

## How does it work?

This is a multi platform kotlin library. This means it can be embedded in kotlin projects for the web, android, ios, java servers, or even wasm or kotlin native projects. It has very few dependencies and all of those are also kotlin multiplatform.

Rankquest assumes you have some sort of API that returns a list of results with ids that you can call. All you need to do to use this framework is:

1. A `SearchPlugin` implementation that fetches results and extracts ids from the results that can be parametrized with a query context (parameters for your search API). Currently rankquest-core has several bundled search plugins that may already cover your needs.
2. You need to register your plugin with the `PluginRegistry` so ranquest-studio and rankquest-cli know how to use your plugin. 
3. A `SearchConfiguration` that describes how to configure your plugin, a list of `MetricConfiguration`. and a description of your search context. 
4. A list of `RatedSearch` objects with rated results for a given search context.

Rankquest Studio allows you to create your configuration and rated searches and allows you to export those in json format. The model classes (kotlinx-serialization) for this are part of rankquest-core. Rankquest-cli of course uses the same model classes. To use all this:

- Use the `pluginType` in your configuration to lookup a the `PluginFactory` for your search plugin.
- Call `pluginFactory.create(configuration)` to instantiate your plugin.
- Call `plugin.runMetrics(configuration, testCases, chunkSize)` to produce a list of `MetricsOutput` (one for each of your `MetricConfiguration`).

Refer to the tests, rankquest-cli source code, or the rankquest-studio source code for more details and examples of how to use this library.

## Goals

- Keep this library simple and flexible enough that people can get started with it quickly. I am a big fan of not overengineering things.
- Enable building ranking test suites without a lot of infrastructure. That's why rankquest studio has no server or database.
- Build out tooling and UIs around this to make this as easy as possible to manage. 
- Remove excuses for people to not do search rank testing
- Keep the code base portable and multiplatform.
