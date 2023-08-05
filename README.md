Rankquest is a simple rank evalution toolset that can be used to benchmark any API that returns search results.

## Why?

The rank eval API in Elasticsearch is nice but it is too low level. Some People use Elasticsearch
to implement search solutions. But of course there are many alternatives. Also, when testing and benchmarking search solutions, you want to do this in black box style and test APIs from the outside instead of benchmarking a specific query in Elasticsearch.

Rankquest addresses this by being implementation neutral. It works with anything that returns some kind of ordered list of results.

Making this a Kotlin multi platform library enables a few nice features, including embedding this in a kotlin-js web application where it can run entirely in the browser. One of my ambitions with this is developing a browser based application that can be used with any kind of search REST API to evaluate it's ranking. With some simple import/export tools for the queries and ratings, this could run stand alone with very little effort.

## Development status

This is very much a work in progress. My goal is to evolve it and have a simple set of tools that 
you can work with on the command line, via docker, or via kotlin based web UIs.

Currently, it has simple implementations similar to what elasticsearch provides in it's `rank_eval` API that can be applied to manually rates searches that are produced by an implementation of the `SearchPlugin` interface.

## How?

This is a multi platform kotlin library. It assumes you have some sort of API that returns a list of results with ids that you can call. All you need to do to use this framework is

1. Provide a `SearchPlugin` implementation that fetches results and extracts ids from the results that can be parametrized with query context (parameters for your API)
2. Provide some ratings for your list of query contexts (one for each search)

That's it. The rest should just work.

## Goals

- Keep this simple and flexible enough that people can get started with it quickly. 
- Enable building ranking test suites without a lot of infrastructure.
- Build out tooling and UIs around this to make this as easy as possible to manage.