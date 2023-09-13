Rankquest is a kotlin multiplatform rank evalution library that can be used to benchmark any API that returns search results.

## Rankquest Studio

Rankquest powers [Rankquest Studio](https://rankquest.jillesvangurp.com). This is a nice web UI for creating and
evaluating test cases that uses rankquest core. Using it require no installation. Everything happens in the browser and your configuration and test cases are stored locally in the browser. Like rankquest-core, Rankquest Studio is open source and you can find the source code [here](https://github.com/jillesvangurp/rankquest-studio).

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

Making this a Kotlin multi platform library enables a few nice features, including embedding this in a kotlin-js web application where it can run entirely in the browser. One of my ambitions with this is developing a browser based application that can be used with any kind of search REST API to evaluate it's ranking. With some simple import/export tools for the queries and ratings, this could run stand alone with very little effort.

## How?

This is a multi platform kotlin library. It assumes you have some sort of API that returns a list of results with ids that you can call. All you need to do to use this framework is

1. Provide a `SearchPlugin` implementation that fetches results and extracts ids from the results that can be parametrized with query context (parameters for your API)
2. Provide some ratings for your list of query contexts (one for each search)

That's it. The rest should just work.

## Goals

- Keep this simple and flexible enough that people can get started with it quickly. 
- Enable building ranking test suites without a lot of infrastructure.
- Build out tooling and UIs around this to make this as easy as possible to manage.