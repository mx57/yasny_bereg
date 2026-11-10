package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val MerriweatherFontFamily = FontFamily(
    Font(R.font.merriweather, FontWeight.Normal),
    Font(R.font.merriweather, FontWeight.Bold)
)

val OpenSansFontFamily = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal),
    Font(R.font.open_sans, FontWeight.Medium),
    Font(R.font.open_sans, FontWeight.Bold)
)

fun buildAppTypography(elderlyLargeFont: Boolean): Typography {
    val scale = if (elderlyLargeFont) 1.22f else 1.0f
    return Typography(
        displayLarge = TextStyle(
            fontFamily = MerriweatherFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * scale).sp,
            lineHeight = (44 * scale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = MerriweatherFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = MerriweatherFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = MerriweatherFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * scale).sp,
            lineHeight = (28 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (19 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (17 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (17 * scale).sp,
            lineHeight = (25 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (15 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * scale).sp,
            lineHeight = (18 * scale).sp
        )
    )
}
