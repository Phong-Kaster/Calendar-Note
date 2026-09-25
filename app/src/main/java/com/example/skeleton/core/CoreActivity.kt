package com.example.skeleton.core

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.example.skeleton.ui.util.NetworkUtil
import com.example.skeleton.ui.util.SystemBarUtil

/**
 * Base activity of the app. It draws edge-to-edge with transparent system bars that always use
 * the dark style (light icons), because the app is always dark — even when the phone is in light mode.
 * @author Phong-Kaster
 */
open class CoreActivity() : AppCompatActivity() {

    /**
     * The Compose entry point a child activity overrides to draw its screen.
     * @author Phong-Kaster
     */
    @Composable
    open fun ComposeView() { }

    /**
     * Turns on edge-to-edge with dark-style (light-icon) transparent bars, then sets the Compose content.
     * @author Phong-Kaster
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        SystemBarUtil.hideNavigationBar(window = this.window)
        setContent { ComposeView() }
    }

    /**
     * Answers: does the phone have a working internet connection right now?
     * @author Phong-Kaster
     */
     fun isInternetConnected(): Boolean {
        return NetworkUtil.isInternetConnected(context = this)
    }
}
