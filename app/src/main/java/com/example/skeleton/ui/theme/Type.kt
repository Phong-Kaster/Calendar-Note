package com.example.skeleton.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.skeleton.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val InterFontFamily = FontFamily(
    Font(R.font.inter_black, FontWeight.Black),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extra_bold, FontWeight.ExtraBold),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_extra_light, FontWeight.ExtraLight),
    Font(R.font.inter_thin, FontWeight.Thin),
)

/**
 * The house text style. Use it for every `Text`; never `MaterialTheme.typography`.
 *
 * @param fontSize in sp. @param fontWeight 400 body, 500 emphasis, 600 titles, 700 section heads.
 * @param lineHeight in sp; defaults to 1.5x the font size.
 * @param color defaults to [ColorTextPrimaryDark] — the same token the theme uses for
 *   `onBackground`/`onSurface`, so an uncoloured `Text` is painted from the palette rather than
 *   from a `Color.White` literal. Same signature, same rendered value as before.
 * @author Phong-Kaster
 */
fun customizedTextStyle(
    fontSize: Int = 14,
    fontWeight: Int = 400,
    lineHeight: Int = (fontSize * 1.5f).toInt(),
    color: Color = ColorTextPrimaryDark,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily = InterFontFamily,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontSize = fontSize.sp,
    fontWeight = FontWeight(fontWeight),
    lineHeight = lineHeight.sp,
    color = color,
    textDecoration = textDecoration
)
