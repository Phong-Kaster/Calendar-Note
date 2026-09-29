package com.example.skeleton.ui.component

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import kotlinx.coroutines.delay

/**
 * A simple top bar: optional round icon button on the left, optional title in the middle,
 * optional round icon button on the right. Every colour comes from the theme, so the bar is
 * readable on the app's dark ground.
 *
 * Example:
 * ```kotlin
 * CoreTopBar(
 *     title = stringResource(R.string.now_playing),
 *     leftIcon = R.drawable.ic_back,
 *     leftContentDescription = stringResource(R.string.back),
 *     onClickLeft = { safeNavigateUp() },
 * )
 * ```
 *
 * @param title text in the middle; null hides it.
 * @param leftIcon drawable of the left button; null hides the button.
 * @param leftBackground tint of the left icon (kept under its old name so older callers compile).
 * @param rightIcon drawable of the right button; null hides the button.
 * @param rightBackground tint of the right icon (kept under its old name so older callers compile).
 * @param onClickLeft called when the left button is tapped.
 * @param onClickRight called when the right button is tapped.
 * @param leftContentDescription what a screen reader says for the left button.
 * @param rightContentDescription what a screen reader says for the right button.
 * @author Phong-Kaster
 */
@Composable
fun CoreTopBar(
    title: String? = null,
    @DrawableRes leftIcon: Int? = null,
    @ColorRes leftBackground: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    @DrawableRes rightIcon: Int? = null,
    @ColorRes rightBackground: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClickLeft: () -> Unit = {},
    onClickRight: () -> Unit = {},
    leftContentDescription: String? = null,
    rightContentDescription: String? = null,
) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .dynamicStatusBarPadding()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .padding(16.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = customizedTextStyle(
                        fontSize = 18,
                        fontWeight = 600,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }


            if (leftIcon != null) {
                IconButton(
                    onClick = {
                        onClickLeft()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        painter = painterResource(id = leftIcon),
                        contentDescription = leftContentDescription,
                        tint = leftBackground,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }


            if (rightIcon != null) {
                IconButton(
                    onClick = {
                        onClickRight()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                        .clip(shape = CircleShape)
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        painter = painterResource(id = rightIcon),
                        contentDescription = rightContentDescription,
                        tint = rightBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

}

private var maximumStatusBarHeight by mutableStateOf(0.dp)

/**
 * Pads the top of a view by the tallest status bar height seen so far, so a top bar never jumps
 * when the status bar is briefly hidden. While the status bar is hidden the padding area is
 * painted black after one second.
 *
 * Example:
 * ```kotlin
 * Spacer(modifier = Modifier.fillMaxWidth().dynamicStatusBarPadding())
 * ```
 *
 * @return the modifier with the background and top padding applied.
 * @author Phong-Kaster
 */
@Composable
fun Modifier.dynamicStatusBarPadding(): Modifier = this.composed {
    // Current status bar height
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var backgroundColor by remember { mutableStateOf(Color.Transparent) }

    // Status bar hidden: wait one second, then paint the area black
    LaunchedEffect(statusBarHeight) {
        if (statusBarHeight == 0.dp) {
            delay(1000)
            backgroundColor = Color.Black
        } else {
            backgroundColor = Color.Transparent
        }
    }

    // Remember the tallest status bar height we have seen
    LaunchedEffect(statusBarHeight) {
        if (statusBarHeight > maximumStatusBarHeight) maximumStatusBarHeight = statusBarHeight
    }

    Modifier
        .background(color = backgroundColor)
        .padding(top = maximumStatusBarHeight)
}

@Preview
@Composable
fun PreviewBasicTopBarWithBackButton() {
    MyApplicationTheme(
        content = {
            CoreTopBar(
                title = "Phong-Kaster",
                leftIcon = R.drawable.ic_back,
                rightIcon = R.drawable.ic_forward
            )
        }
    )
}
