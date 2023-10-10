package com.jilesvangurp.rankquest.core

import io.kotest.matchers.doubles.shouldBeLessThan
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random
import kotlin.test.Test

class StatsTest {
    @Test
    fun shouldCalculateStats() {
        // guarantee same outcome every time
        val random = Random(666)
        val scores = (0..10000).map { random.nextDouble(0.0, 1.0) }

        val stats = scores.stats

        stats.min shouldBeLessThan stats.mean
        stats.mean shouldBeLessThan stats.max
        println(stats)
        // mean should be ballpark in the middle
        abs(stats.mean - 0.5) shouldBeLessThan 0.1
        // 2x standard deviation should be about 2/3 rds
        abs(stats.standardDeviation * 2 - 0.66) shouldBeLessThan 0.1

        // variance is the square of the standard deviation
        abs(stats.variance - stats.standardDeviation.pow(2)) shouldBeLessThan 0.000001
    }
}