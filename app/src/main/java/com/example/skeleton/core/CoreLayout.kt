package com.example.skeleton.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp


/**
 * A ready-made screen frame (a `Scaffold`) that every screen draws inside.
 * The background is always the theme's `background` colour and default content colour is
 * `onBackground`, so text stays readable on the dark ground.
 *
 * @param showLoading when true, a spinner is shown instead of [content].
 * @param contentWindowInsets insets are zero by default; screens pad for system bars themselves.
 * @author Phong-Kaster
 */
@Composable
fun CoreLayout(
    modifier: Modifier = Modifier,
    showLoading: Boolean = false,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    content: @Composable () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            // Ground colour comes from the theme so every screen shares one readable background.
            .background(color = MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = contentWindowInsets,
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (showLoading) {
                CircularProgressIndicator()
            } else {
                content()
            }
        }
    }
}