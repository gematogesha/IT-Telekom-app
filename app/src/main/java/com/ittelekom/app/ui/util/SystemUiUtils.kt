package com.ittelekom.app.ui.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetSystemBarsColor(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    useDarkIcons: Boolean = !isSystemInDarkTheme()
) {
    val systemUiController = rememberSystemUiController()

    // Обеспечиваем использование актуальных значений внутри DisposableEffect
    val currentStatusBarColor by rememberUpdatedState(newValue = statusBarColor)
    val currentNavigationBarColor by rememberUpdatedState(newValue = navigationBarColor)
    val currentUseDarkIcons by rememberUpdatedState(newValue = useDarkIcons)

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = currentStatusBarColor,
            darkIcons = currentUseDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = currentNavigationBarColor,
            darkIcons = currentUseDarkIcons
        )

        onDispose {}
    }
}
