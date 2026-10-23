package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MonochromeColorScheme = darkColorScheme(
    primary = MonochromeWhite,
    onPrimary = MonochromeBlack,
    primaryContainer = MonochromeMediumGrey,
    onPrimaryContainer = MonochromeWhite,
    secondary = MonochromeLightGrey,
    onSecondary = MonochromeBlack,
    secondaryContainer = MonochromeDarkSurface,
    onSecondaryContainer = MonochromeWhite,
    tertiary = MonochromeWhite,
    onTertiary = MonochromeBlack,
    background = MonochromeBlack,
    onBackground = MonochromeWhite,
    surface = MonochromeDarkSurface,
    onSurface = MonochromeWhite,
    surfaceVariant = MonochromeCardSurface,
    onSurfaceVariant = MonochromeMutedText,
    outline = MonochromeCardBorder,
    outlineVariant = MonochromeMediumGrey,
    error = MonochromeWhite,
    onError = MonochromeBlack
)

@Composable
fun AutoIoTTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Pure Black & White theme as requested
    MaterialTheme(
        colorScheme = MonochromeColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    AutoIoTTheme(darkTheme = true, dynamicColor = false, content = content)
}
