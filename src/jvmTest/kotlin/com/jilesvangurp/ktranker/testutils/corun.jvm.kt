package com.jilesvangurp.ktranker.testutils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

actual fun coRun(timeout: Duration, block: suspend () -> Unit) {
    runBlocking {
        withTimeout(timeout) {
            block.invoke()
        }
    }
}
