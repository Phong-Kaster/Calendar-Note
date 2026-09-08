package com.example.skeleton.core.extension.null_safety

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object NullSafetyExtension {
    fun <T> T?.orDefault(default: T): T {
        return this ?: default
    }

    fun <T> T?.orThrow(exception: () -> Exception): T {
        return this ?: throw exception()
    }
    fun <T> T?.ifNull(action: () -> Unit): T? {
        if (this == null) action()
        return this
    }
    fun <T> T?.ifNotNull(action: (T) -> Unit): T? {
        if (this != null) action(this)
        return this
    }

    // Usage
//    val username: String? = null
//    val display = username.orDefault("Guest") // "Guest"
//    val userId: String? = null
//    val id = userId.orThrow { IllegalArgumentException("User ID required") }
//    var errorShown = false
//    val result: String? = null
//    result.ifNull { errorShown = true }
//    val user: User? = getUser()
//    user.ifNotNull { println("Found user: ${it.name}") }
}