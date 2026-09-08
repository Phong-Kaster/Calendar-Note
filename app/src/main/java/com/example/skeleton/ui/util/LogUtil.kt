package com.example.skeleton.ui.util

import android.util.Log

object LogUtil {
    fun logcat(
        tag: String = "Jetpack Compose",
        message: String,

        enableDivider: Boolean = false
    ) {
//        val allow = if (BuildConfig.DEBUG) true else false
//
//        if (!allow) return


        if (enableDivider) {
            Log.d(tag, "-> $message ----------------------------")
        }
        Log.d(tag, "-> message = $message")

    }
}