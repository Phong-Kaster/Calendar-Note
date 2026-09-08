package com.example.skeleton.core.extension.date_and_time

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object LocalDateExtension {
    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.formatTo(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return format(DateTimeFormatter.ofPattern(pattern))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.isWeekend(): Boolean {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDate.isWeekday(): Boolean {
        return !isWeekend()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toEpochMillis(): Long {
        return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Long.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    }
    // Usage
//    val now = LocalDateTime.now()
//    val display = now.formatTo("MMM dd, yyyy HH:mm") // "Jan 08, 2026 15:30"
//    val date = LocalDate.now()
//    if (date.isWeekend()) {
//        println("It's the weekend!")
//    }
//    val timestamp = System.currentTimeMillis()
//    val dateTime = timestamp.toLocalDateTime()
}