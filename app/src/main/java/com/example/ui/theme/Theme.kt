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

private val DarkColorScheme = darkColorScheme(
    primary = NaturalDarkPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = NaturalDarkPrimaryContainer,
    onPrimaryContainer = NaturalDarkOnPrimaryContainer,
    secondary = NaturalDarkPrimary,
    onSecondary = Color.Black,
    background = NaturalDarkBg,
    onBackground = NaturalDarkTextPrimary,
    surface = NaturalDarkSurface,
    onSurface = NaturalDarkTextPrimary,
    surfaceVariant = NaturalDarkSurfaceVariant,
    onSurfaceVariant = NaturalDarkTextMuted,
    error = NaturalAlertRedText
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalEmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = NaturalEmeraldContainer,
    onPrimaryContainer = NaturalEmeraldPrimary,
    secondary = NaturalEmeraldLight,
    onSecondary = Color.White,
    background = NaturalSageBg,
    onBackground = Slate900,
    surface = NaturalSurface,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = NaturalAlertRedText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
