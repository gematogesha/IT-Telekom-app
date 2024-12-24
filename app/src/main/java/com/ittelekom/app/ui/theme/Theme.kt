package com.ittelekom.app.ui.theme

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat

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
    outlineVariant = OutlineVariant,
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
    window: android.view.Window? = null,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (ThemeManager.currentTheme.value) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    window?.apply {
        statusBarColor = colorScheme.surface.toArgb()
        navigationBarColor = colorScheme.surfaceContainer.toArgb()

        val isLightStatusBar = colorScheme.surface.luminance() > 0.5f
        val isLightNavBar = colorScheme.surfaceContainer.luminance() > 0.5f

        val windowInsetsController = WindowInsetsControllerCompat(this, decorView)
        windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
        windowInsetsController.isAppearanceLightNavigationBars = isLightNavBar
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun LoginActivityTheme(
    window: android.view.Window? = null,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (ThemeManager.currentTheme.value) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        isDarkTheme -> LoginDarkColorScheme
        else -> LoginLightColorScheme
    }

    window?.apply {
        statusBarColor = colorScheme.surface.toArgb()
        navigationBarColor = colorScheme.surfaceContainer.toArgb()

        val isLightStatusBar = colorScheme.surface.luminance() > 0.5f
        val isLightNavBar = colorScheme.surfaceContainer.luminance() > 0.5f

        val windowInsetsController = WindowInsetsControllerCompat(this, decorView)
        windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
        windowInsetsController.isAppearanceLightNavigationBars = isLightNavBar
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}