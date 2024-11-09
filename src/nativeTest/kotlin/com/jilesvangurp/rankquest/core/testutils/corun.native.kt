package com.jilesvangurp.rankquest.core.testutils

import kotlin.time.Duration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

actual fun coRun(timeout: Duration, block: suspend () -> Unit) {
    runBlocking {
        withTimeout(timeout) {
            block.invoke()
        }
    }
}