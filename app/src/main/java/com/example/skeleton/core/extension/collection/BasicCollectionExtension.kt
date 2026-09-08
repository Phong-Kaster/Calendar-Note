package com.example.skeleton.core.extension.collection

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object BasicCollectionExtension {
    // Usage
//    val numbers = listOf(1, 2, 3, 4, 5)
//    val secondNum = numbers.secondOrNull() // 2
//    val empty = emptyList<String>()
//    empty.takeIfNotEmpty() // null
//    val (evens, odds) = numbers.split { it % 2 == 0 }
//    // evens = [2, 4], odds = [1, 3, 5]
//    val files = listOf(
//        File("file1.txt", 100L),
//        File("file2.txt", 200L)
//    )
//    val totalSize = files.sumByLong { it.size } // 300L
    fun <T> List<T>.second(): T {
        if (size < 2) throw NoSuchElementException("List has less than 2 elements")
        return this[1]
    }

    fun <T> List<T>.secondOrNull(): T? {
        return if (size >= 2) this[1] else null
    }
    fun <T> List<T>.takeIfNotEmpty(): List<T>? {
        return if (isNotEmpty()) this else null
    }
    fun <T> List<T>.split(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
        return partition(predicate)
    }
    fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
        return fold(0L) { sum, element -> sum + selector(element) }
    }

}