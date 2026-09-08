package com.example.skeleton.core.extension.number

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object RangeAndBoundaryExtensions {
    // Usage
//    val value = 150
//    val clamped = value.clamp(0, 100) // 100
//    val progress = 75
//    if (progress.inRange(50..100)) {
//        println("More than halfway")
//    }
//    val completed = 30
//    val total = 100
//    val percentage = completed.toPercentage(total) // 30.0

    fun Int.clamp(min: Int, max: Int): Int {
        return when {
            this < min -> min
            this > max -> max
            else -> this
        }
    }

    fun Float.clamp(min: Float, max: Float): Float {
        return when {
            this < min -> min
            this > max -> max
            else -> this
        }
    }

    fun Int.inRange(range: IntRange): Boolean {
        return this in range
    }

    fun Int.toPercentage(total: Int): Float {
        return if (total == 0) 0f else (this.toFloat() / total) * 100
    }

}