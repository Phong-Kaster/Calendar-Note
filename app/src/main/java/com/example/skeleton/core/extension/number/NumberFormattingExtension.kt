package com.example.skeleton.core.extension.number

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object NumberFormattingExtension {
    // Usage
//    val number = 1234567
//    val formatted = number.formatWithCommas() // "1,234,567"
//    val price = 49.99
//    val display = price.formatAsPrice() // "$49.99"
//    val value = 3.14159f
//    val rounded = value.roundTo(2) // 3.14f
//    val flag = 1
//    if (flag.toBoolean()) {
//        // Execute
//    }

    fun Int.formatWithCommas(): String {
        return String.format("%,d", this)
    }

    fun Double.formatAsPrice(currencySymbol: String = "$"): String {
        return "$currencySymbol%.2f".format(this)
    }
    fun Float.roundTo(decimals: Int): Float {
        val multiplier = 10.0.pow(decimals)
        return (this * multiplier).roundToInt() / multiplier.toFloat()
    }
    fun Int.toBoolean(): Boolean = this != 0
    fun Boolean.toInt(): Int = if (this) 1 else 0
    fun Int.isEven(): Boolean = this % 2 == 0
    fun Int.isOdd(): Boolean = !isEven()

}