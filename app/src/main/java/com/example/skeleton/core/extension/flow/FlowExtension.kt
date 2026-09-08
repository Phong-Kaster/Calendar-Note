package com.example.skeleton.core.extension.flow

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object FlowExtension {

    // Usage
//    searchQueryFlow
//    .debounce(300)
//    .filterNotNull()
//    .collect { query ->
//        performSearch(query)
//    }

//    clickFlow
//    .throttleFirst(1000) // Prevent double clicks
//    .collect {
//        handleClick()
//    }

//    itemsFlow
//    .filterNotEmpty()
//    .collect { items ->
//        displayItems(items)
//    }

    fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
        var lastEmissionTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmissionTime >= windowDuration) {
                lastEmissionTime = currentTime
                emit(value)
            }
        }
    }

    fun <T> Flow<List<T>>.filterNotEmpty(): Flow<List<T>> {
        return filter { it.isNotEmpty() }
    }
    fun <T> Flow<T?>.filterNotNull(): Flow<T> {
        return mapNotNull { it }
    }
    @OptIn(FlowPreview::class)
    fun <T> Flow<T>.onEachDebounce(timeoutMillis: Long, action: suspend (T) -> Unit): Flow<T> {
        return debounce(timeoutMillis).onEach { action(it) }
    }

}