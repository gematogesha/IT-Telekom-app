package com.example.it_telekom_app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    surface = Surface,
    onSurface = OnSurface,
    surfaceContainer = Container,
    secondaryContainer = PrimaryContainer,
    onSecondaryContainer = OnPrimaryContainer,
    onSurfaceVariant = Outline,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainer = DarkContainer,
    secondaryContainer = DarkPrimary,
    onSecondaryContainer = DarkOnPrimary,
    onSurfaceVariant = DarkOutline,
)


//Login Activity
private val LoginLightColorScheme = lightColorScheme(
    primary = LoginPrimary,
    onPrimary = LoginOnPrimary,
    surface = LoginSurface,
    onSurface = LoginOnSurface,
    primaryContainer = LoginOnSurface,
    secondaryContainer = LoginOnSurface70,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface

)

private val LoginDarkColorScheme = darkColorScheme(
    primary = DarkLoginPrimary,
    onPrimary = DarkLoginOnPrimary,
    surface = DarkLoginSurface,
    onSurface = DarkLoginOnSurface,
    primaryContainer = DarkLoginOnSurface,
    secondaryContainer = DarkLoginOnSurface70,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface
)

@Composable
fun ITTelekomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

@Composable
fun LoginActivityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> LoginDarkColorScheme
        else -> LoginLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}