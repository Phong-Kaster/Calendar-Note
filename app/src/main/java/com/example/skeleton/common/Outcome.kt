package com.example.skeleton.common

/**
 * Represents the outcome of an async operation with three states.
 * Use [Loading] while the operation is in progress, [Success] when it completes,
 * and [Error] when it fails. Emitted via [safeApiCallFlow] for API-backed flows.
 * @author Phong-Kaster
 */
sealed class Outcome<out T> {

    data object Loading : Outcome<Nothing>()

    data class Success<T>(val data: T) : Outcome<T>()

    data class Error(val message: String, val throwable: Throwable? = null) : Outcome<Nothing>()
}
