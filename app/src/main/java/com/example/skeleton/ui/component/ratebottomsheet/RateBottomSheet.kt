package com.example.skeleton.ui.component.ratebottomsheet


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.skeleton.ui.component.ratebottomsheet.RateOption.Companion.getByPosition
import com.example.skeleton.R
import com.example.skeleton.ui.component.CoreBottomSheet
import com.example.skeleton.ui.modifier.outerShadow
import com.example.skeleton.ui.theme.customizedTextStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateBottomSheet(
    enabled: Boolean = false,
    onDismiss: () -> Unit = {},
    onSubmit: (Int, String) -> Unit = { _, _ -> },
) {
    CoreBottomSheet(
        enable = enabled,
        containerColor = Color(0xFF2E2E2E),
        onDismissRequest = onDismiss,
        content = {
            RateBottomSheetLayout(
                onDismiss = onDismiss,
                onSubmit = onSubmit,
            )
        }
    )
}

@Composable
fun RateBottomSheetLayout(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onSubmit: (Int, String) -> Unit = { _, _ -> },
) {
    val compositionStars by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.animation_star)
    )


    // feedback
    var feedbackStar by remember { mutableIntStateOf(5) }
    var feedbackMessage by remember { mutableStateOf("") }

    // focus manager
    val focusManager = LocalFocusManager.current
    var focusOn by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF2E2E2E))
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp, bottom = 16.dp,
                    start = 16.dp, end = 16.dp
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .size(24.dp)
            )
        }


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .background(color = Color(0xFF2E2E2E))
                .padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            // Emoji animation
            AnimatedContent(
                targetState = feedbackStar,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .scale(1.25f),

                ) { targetStars ->

                val rawRes = remember(targetStars) { getByPosition(position = targetStars) }
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(resId = rawRes)
                )

                if (composition == null) return@AnimatedContent

                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(1.25f)
                )
            }


            // Do you like the app?
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.do_you_like_the_app),
                style = customizedTextStyle(
                    fontSize = 18,
                    fontWeight = 600,
                    color = Color.White,
                )
            )

            // We are glad to hear that you love our app
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.we_are_thrilled_to_hear_that_you_love_our_app),
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 400,
                    color = Color(0xFF919191)
                )
            )

            // Select the number of 5 stars you would like to give
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(feedbackStar) { index ->

                    val progress by animateLottieCompositionAsState(
                        composition = compositionStars,
                        iterations = 1,
                        restartOnPlay = true
                    )

                    LottieAnimation(
                        composition = compositionStars,
                        progress = { progress },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(1.2f)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                feedbackStar = index + 1
                                focusManager.clearFocus()
                            }
                    )
                }

                // ⭐ Empty stars (static)
                repeat((5 - feedbackStar).coerceAtLeast(0)) {
                    Image(
                        painter = painterResource(R.drawable.img_star_unfill),
                        contentDescription = "Lottie animation",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                feedbackStar = feedbackStar + it + 1
                                focusManager.clearFocus()
                            }
                    )
                }
            }


            // Describe your feedback
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .outerShadow(
                        shadowRadius = 6.dp,
                        spreadRadius = 4.dp,
                        shadowColor = Color.Black.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color(0xFF424242))
                    .border(
                        width = 1.5.dp,
                        color = if (focusOn) Color(0xFF683FEA) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .clickable { focusRequester.requestFocus() }
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusOn = it.isFocused }
                    .padding(vertical = 16.dp, horizontal = 14.dp),
            ) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    cursorBrush = SolidColor(value = Color(0xFF683FEA)),
                    value = feedbackMessage,
                    onValueChange = { if (feedbackMessage.length < 500) feedbackMessage = it },
                    textStyle = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 400,
                        color = Color.White,
                    ),
                    maxLines = 5,
                    minLines = 3,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    decorationBox = { innerTextField ->
                        if (feedbackMessage.isEmpty()) {
                            Text(
                                text = "${stringResource(R.string.describe_your_feedback)}...",
                                style = customizedTextStyle(
                                    fontSize = 15,
                                    fontWeight = 400,
                                    color = Color(0xFF919191)
                                ),
                            )
                        }
                        innerTextField()
                    }
                )
                Text(
                    text = "${feedbackMessage.length}/500",
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 300,
                        color = Color(0xFF919191)
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // button submit
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onSubmit(feedbackStar, feedbackMessage)
                    }
                    .background(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Blue,
                    )
                    .padding(vertical = 14.dp)

            ) {
                Text(
                    text = stringResource(R.string.submit),
                    style = customizedTextStyle(
                        fontSize = 18,
                        fontWeight = 500,
                        color = Color.White,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewRateBottomSheetLayout() {
    RateBottomSheetLayout()
}