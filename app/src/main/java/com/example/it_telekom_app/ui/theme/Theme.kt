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
    onPrimary = OnPrimary,
    inversePrimary = InversePrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    surface = Surface,
    onSurface = OnSurface,
    surfaceContainer = Container,

    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,
    outlineVariant = OutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    inversePrimary = DarkInversePrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainer = DarkContainer,

    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,

    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)


//Login Activity
private val LoginLightColorScheme = lightColorScheme(
    primary = LoginPrimary,
    onPrimary = LoginOnPrimary,

    primaryContainer = LoginOnSurface,

    surface = LoginSurface,
    onSurface = LoginOnSurface,
    onSurfaceVariant = LoginOsSurfaceVariant,

    outline = LoginOutline,
)

private val LoginDarkColorScheme = darkColorScheme(
    primary = DarkLoginPrimary,
    onPrimary = DarkLoginOnPrimary,

    primaryContainer = DarkLoginOnSurface,

    surface = DarkLoginSurface,
    onSurface = DarkLoginOnSurface,
    onSurfaceVariant = DarkLoginOnSurfaceVariant,

    outline = DarkLoginOutline,
)

@Composable
fun ITTelekomTheme(
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

@Composable
fun LoginActivityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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