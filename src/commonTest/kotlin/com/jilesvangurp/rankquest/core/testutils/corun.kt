package com.jilesvangurp.rankquest.core.testutils

import kotlinx.coroutines.test.TestResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

expect fun coRun(timeout: Duration = 30.seconds, block: suspend () -> Unit): TestResult
