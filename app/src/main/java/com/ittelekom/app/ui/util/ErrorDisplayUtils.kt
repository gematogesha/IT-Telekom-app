package com.ittelekom.app.ui.util

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4


@Composable
fun ErrorDisplay(
    errorMessage: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 170.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.inverseOnSurface,
    defaultErrorMessage: String = "Ошибка загрузки данных"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getErrorIcon(errorMessage),
                tint = iconTint,
                contentDescription = "Error Icon",
                modifier = Modifier
                    .size(iconSize)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage ?: defaultErrorMessage,
                style = textStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun getErrorIcon(errorMessage: String?): ImageVector {
    return when (errorMessage) {
        "Нет подключения к интернету" -> Icons.Rounded.SignalWifiConnectedNoInternet4
        else -> Icons.Outlined.CloudOff
    }
}