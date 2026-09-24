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

open class CoreActivity() : AppCompatActivity() {

    @Composable
    open fun ComposeView() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark (see MyApplicationTheme), so the status-bar clock and icons
        // must always be drawn light, even when the phone itself is in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
        )
        SystemBarUtil.hideNavigationBar(window = this.window)
        setContent { ComposeView() }
    }

     fun isInternetConnected(): Boolean {
        return NetworkUtil.isInternetConnected(context = this)
    }
}