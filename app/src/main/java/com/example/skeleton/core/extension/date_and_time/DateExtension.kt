package com.example.skeleton.core.extension.date_and_time

import android.icu.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * [Kotlin Extension Functions That Will Clean Your Code](https://trricho.medium.com/android-10-kotlin-extension-functions-that-will-clean-your-code-f9123f1938bc)
 */
object DateExtension {
    fun Date.formatTo(pattern: String = "yyyy-MM-dd"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(this)
    }

    fun Date.toCalendar(): Calendar {
        return Calendar.getInstance().apply {
            time = this@toCalendar
        }
    }
    fun Date.addDays(days: Int): Date {
        return toCalendar().apply {
            add(Calendar.DAY_OF_MONTH, days)
        }.time
    }
    fun Date.addHours(hours: Int): Date {
        return toCalendar().apply {
            add(Calendar.HOUR_OF_DAY, hours)
        }.time
    }
    fun Date.isToday(): Boolean {
        val today = Calendar.getInstance()
        val dateCalendar = toCalendar()
        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }
    fun Date.isFuture(): Boolean {
        return time > System.currentTimeMillis()
    }
    fun Date.isPast(): Boolean {
        return time < System.currentTimeMillis()
    }
    // Usage
//    val now = Date()
//    val formatted = now.formatTo("MMM dd, yyyy") // "Jan 08, 2026"
//    val tomorrow = now.addDays(1)
//    val inThreeHours = now.addHours(3)
//    if (deadline.isFuture()) {
//        println("Still have time!")
//    }
}