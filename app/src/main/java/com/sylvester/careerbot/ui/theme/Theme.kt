package com.sylvester.careerbot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom Colors
val Blue80 = Color(0xFF4FC3F7)
val BlueGrey80 = Color(0xFF90A4AE)
val Teal80 = Color(0xFF4DB6AC)

val Blue40 = Color(0xFF0288D1)
val BlueGrey40 = Color(0xFF546E7A)
val Teal40 = Color(0xFF00897B)

// Additional custom colors
val LightBlueBackground = Color(0xFFF5F9FD)
val DarkBlueBackground = Color(0xFF0D1117)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004880),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = Teal80,
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF005144),
    onSecondaryContainer = Color(0xFF70F7DC),

    tertiary = BlueGrey80,
    onTertiary = Color(0xFF1F333C),
    tertiaryContainer = Color(0xFF354A53),
    onTertiaryContainer = Color(0xFFD5E4EC),

    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkBlueBackground,
    onBackground = Color(0xFFE1E2E8),

    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CE),

    outline = Color(0xFF8C9198),
    outlineVariant = Color(0xFF42474E),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E2E8),
    inverseOnSurface = Color(0xFF2E3135),
    inversePrimary = Color(0xFF0061A4),
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF70F7DC),
    onSecondaryContainer = Color(0xFF002117),

    tertiary = BlueGrey40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5E4EC),
    onTertiaryContainer = Color(0xFF0C1F28),

    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),

    background = LightBlueBackground,
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF42474E),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC2C7CE),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E3135),
    inverseOnSurface = Color(0xFFF0F0F6),
    inversePrimary = Color(0xFF9ECAFF),
)

@Composable
fun CareerBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false for custom theme
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}