Simple rank evalution toolset similar to the rank eval API in Elasticsearch 
but written such that it can be used with anything that returns search results.


## Why?

The rank eval API in Elasticsearch is too low level. People use Elasticsearch
to implement search solutions. However what they need to test is those integrated
solutions (e.g. a search API) and not the underlying implementation details (e.g. the bit that calls Elasticsearch with a query, or some other solution).

## Development status

Work in progress but my goal is to evolve it and have a simple set of tools that 
you can work with on the command line, via docker, or via kotlin based web UIs.

## How?

This is a multi platform kotlin library. It assumes you have some sort of API that returns a list of results with ids. All you need to do to use this framework is

1. Provide an implementation of fetching results and extracting ids from the results that can be parametrized with query context (parameters for your API)
2. Provide some ratings for your list of query contexts (one for each search)

That's it. The rest should just work.

## Goals

- Keep this simple enough that people can get started with it quickly and help them build ranking test suites.
- Build out tooling and UIs around this to make this even easier