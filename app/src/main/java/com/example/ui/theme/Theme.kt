package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = OnForestGreenContainer,
    secondary = WarmAmber,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = SlateTeal,
    onTertiary = Color.White,
    tertiaryContainer = SlateTealContainer,
    onTertiaryContainer = OnSlateTealContainer,
    error = TerracottaAlert,
    onError = Color.White,
    errorContainer = TerracottaContainer,
    onErrorContainer = OnTerracottaContainer,
    background = PaperCream,
    onBackground = InkDark,
    surface = WarmCardSurface,
    onSurface = InkDark,
    surfaceVariant = SandSurface,
    onSurfaceVariant = InkMuted,
    outline = OutlineWarm
)

private val DarkColorScheme = darkColorScheme(
    primary = LightSagePrimary,
    onPrimary = OnForestGreenContainer,
    primaryContainer = DarkForestContainer,
    onPrimaryContainer = ForestGreenContainer,
    secondary = WarmGoldSecondary,
    onSecondary = OnAmberContainer,
    secondaryContainer = Color(0xFF4B3010),
    onSecondaryContainer = AmberContainer,
    tertiary = Color(0xFF90C8DE),
    onTertiary = OnSlateTealContainer,
    tertiaryContainer = Color(0xFF1A3E4C),
    onTertiaryContainer = SlateTealContainer,
    error = CoralAlertDark,
    onError = OnTerracottaContainer,
    errorContainer = Color(0xFF621911),
    onErrorContainer = TerracottaContainer,
    background = DarkPineBg,
    onBackground = CreamTextDark,
    surface = DarkPineCard,
    onSurface = CreamTextDark,
    surfaceVariant = DarkPineSurface,
    onSurfaceVariant = MutedSageTextDark,
    outline = Color(0xFF4B5850)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun YasnyBeregTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    elderlyLargeFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = buildAppTypography(elderlyLargeFont)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes,
        content = content
    )
}
