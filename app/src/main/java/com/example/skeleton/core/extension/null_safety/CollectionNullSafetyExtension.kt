package com.example.skeleton.core.extension.null_safety

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object CollectionNullSafetyExtension {
    fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

    fun <K, V> Map<K, V>?.orEmpty(): Map<K, V> = this ?: emptyMap()
    fun <T> List<T>?.isNullOrEmpty(): Boolean {
        return this == null || isEmpty()
    }
    fun <T> List<T>?.isNotNullOrEmpty(): Boolean {
        return !isNullOrEmpty()
    }
    // Usage
//    val items: List<String>? = null
//    items.orEmpty().forEach { println(it) } // Safe iteration
//    val map: Map<String, Int>? = null
//    val count = map.orEmpty().size // 0
//    if (items.isNotNullOrEmpty()) {
//        // Process items
//    }
}