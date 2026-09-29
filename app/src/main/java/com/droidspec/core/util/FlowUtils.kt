package com.droidspec.core.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Polls [block] every [this] emitted interval (ms). Emissions are deduplicated.
 * Polling stops automatically when the collecting coroutine is cancelled.
 */
fun <T> Flow<Long>.pollLatest(block: suspend () -> T): Flow<T> =
    flatMapLatest { interval ->
        flow {
            while (currentCoroutineContext().isActive) {
                emit(block())
                delay(interval)
            }
        }
    }.distinctUntilChanged()
