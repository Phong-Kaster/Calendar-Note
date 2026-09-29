package com.example.skeleton.core

import android.os.Bundle
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

open class CoreActivity() : AppCompatActivity() {

    @Composable
    open fun ComposeView() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
        )
        // The navigation bar stays visible (and transparent, edge to edge). Hiding it, as the skeleton
        // did, made the navigation-bar inset zero, so bottom controls sat in the system gesture zone and
        // a tap on the mini player pulled the phone's navigation bar up instead.
        setContent { ComposeView() }
    }
}