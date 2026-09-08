package com.example.skeleton.core.extension.collection
/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object AdvancedCollectionExtension {
    // Usage
//    val items = listOf(1, 2, 1, 3, 2, 4)
//    val dups = items.duplicates() // [1, 2]
//    val words = listOf("hello", "world", "hello", "kotlin")
//    val unique = words.replaceAll("hello", "hi") // ["hi", "world", "hi", "kotlin"]

    fun <T> List<T>.replaceAll(oldValue: T, newValue: T): List<T> {
        return map { if (it == oldValue) newValue else it }
    }

    fun <T> List<T>.chunkedBy(predicate: (T) -> Boolean): List<List<T>> {
        val result = mutableListOf<List<T>>()
        var currentChunk = mutableListOf<T>()

        forEach { item ->
            if (predicate(item) && currentChunk.isNotEmpty()) {
                result.add(currentChunk)
                currentChunk = mutableListOf()
            }
            currentChunk.add(item)
        }

        if (currentChunk.isNotEmpty()) {
            result.add(currentChunk)
        }

        return result
    }
    fun <T> List<T>.duplicates(): List<T> {
        return groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .keys
            .toList()
    }

}