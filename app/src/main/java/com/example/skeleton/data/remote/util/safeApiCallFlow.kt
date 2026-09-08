package com.example.skeleton.data.remote.util

import com.example.skeleton.common.Outcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Wraps API calls in a Flow that emits [Outcome] states.
 * Use in repository implementations for consistent error handling.
 * The [Throwable] in Outcome.Error can be mapped to UI messages via [Throwable.toUiMessage].
 */
fun <T> safeApiCallFlow(
    apiCall: suspend () -> T
): Flow<Outcome<T>> = flow {
    emit(Outcome.Loading)

    try {
        emit(Outcome.Success(apiCall()))
    } catch (e: Exception) {
        emit(Outcome.Error(e.message ?: "Unknown error", e))
    }
}.flowOn(Dispatchers.IO)
