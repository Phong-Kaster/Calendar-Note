package com.example.skeleton.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skeleton.ui.theme.MyApplicationTheme

/**
 * Wraps a component in the real theme, on the real ground colour the app paints.
 *
 * Both halves matter. The `@Preview` functions in the main source set call components bare, so
 * they render against Studio's default white and with Material's baseline colours — which is
 * precisely why a screen full of hardcoded `Color.White` looked fine in the preview pane and
 * unreadable nowhere else. A screenshot taken through this scaffold is a screenshot of what a
 * user sees: theme colours, on `colorScheme.background`, the same ground `CoreLayout` paints.
 *
 * @author Phong-Kaster
 */
@Composable
fun ScreenshotScaffold(
    content: @Composable () -> Unit,
) {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
        ) {
            content()
        }
    }
}
