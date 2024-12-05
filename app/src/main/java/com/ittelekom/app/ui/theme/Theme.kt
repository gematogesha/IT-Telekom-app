package com.ittelekom.app.ui.theme

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

    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,

    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    surface = Surface,
    onSurface = OnSurface,
    surfaceContainer = Container,
    surfaceContainerHigh = ContainerHighest,

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

    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,

    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainer = DarkContainer,
    surfaceContainerHigh = DarkContainerHighest,

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
    //theme: String,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // TODO: Реализовать выбор темы
    val theme = "system"
    val isDarkTheme = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> DarkColorScheme
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
    //theme: String,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // TODO: Реализовать выбор темы
    val theme = "system"
    val isDarkTheme = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> LoginDarkColorScheme
        else -> LoginLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}