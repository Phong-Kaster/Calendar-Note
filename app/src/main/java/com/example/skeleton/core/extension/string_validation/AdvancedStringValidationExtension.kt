package com.example.skeleton.core.extension.string_validation

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object AdvancedStringValidationExtension {
    fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
        return if (length <= maxLength) this
        else take(maxLength - ellipsis.length) + ellipsis
    }

    fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
    fun String.removeWhitespace(): String {
        return replace("\\s+".toRegex(), "")
    }
    fun String.toSlug(): String {
        return lowercase()
            .replace("\\s+".toRegex(), "-")
            .replace("[^a-z0-9-]".toRegex(), "")
            .replace("-+".toRegex(), "-")
            .trim('-')
    }


    // Example
    val title = "This is a Long Article Title That Needs Truncation"
    val short = title.truncate(20) // "This is a Long Ar..."
    val name = "john doe"
    val formatted = name.capitalizeWords() // "John Doe"
    val slug = "My Blog Post!".toSlug() // "my-blog-post"
}