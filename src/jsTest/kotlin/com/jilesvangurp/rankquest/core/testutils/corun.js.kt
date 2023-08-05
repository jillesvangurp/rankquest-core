package com.jilesvangurp.rankquest.core.testutils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlin.time.Duration

@OptIn(DelicateCoroutinesApi::class)
actual fun coRun(timeout: Duration, block: suspend () -> Unit): dynamic = GlobalScope.async {
    block.invoke()
}.asPromise()

