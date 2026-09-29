package com.example.skeleton.core

import android.os.Bundle
import androidx.activity.compose.setContent
import android.graphics.Color
import androidx.activity.SystemBarStyle
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