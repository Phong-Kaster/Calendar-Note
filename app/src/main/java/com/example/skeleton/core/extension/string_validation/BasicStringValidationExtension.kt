package com.example.skeleton.core.extension.string_validation

import java.net.URL

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object BasicStringValidationExtension {
    // ✓ Clean validation extensions
    fun String?.isValidEmail(): Boolean {
        if (this == null) return false
        return matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }

    fun String?.isValidPhone(): Boolean {
        if (this == null) return false
        return matches(Regex("^\\+?[1-9]\\d{1,14}$"))
    }

    fun String?.isValidUrl(): Boolean {
        if (this == null) return false
        return try {
            URL(this)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun String.isAlphanumeric(): Boolean {
        return matches(Regex("^[a-zA-Z0-9]+$"))
    }

    fun String.containsDigit(): Boolean {
        return any { it.isDigit() }
    }
}