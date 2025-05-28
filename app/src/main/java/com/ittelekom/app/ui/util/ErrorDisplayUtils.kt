package com.ittelekom.app.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ErrorDisplay(
    refreshFunction: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 170.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outlineVariant,
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: defaultErrorMessage,
                style = textStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    refreshFunction()
                }
            ) {
                Text("Обновить")
            }
        }
    }
}

@Composable
fun getErrorIcon(errorMessage: String?): ImageVector {
    return when (errorMessage) {
        "Нет подключения к интернету" -> Icons.Rounded.SignalWifiConnectedNoInternet4
        "Нет доступных тарифов" -> Icons.Outlined.SentimentDissatisfied
        else -> Icons.Outlined.CloudOff
    }
}

@Composable
fun DevDisplay(
    modifier: Modifier = Modifier,
    iconSize: Dp = 170.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outlineVariant,
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
                imageVector = Icons.Outlined.SettingsSuggest,
                tint = iconTint,
                contentDescription = "Error Icon",
                modifier = Modifier
                    .size(iconSize)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "В разработке",
                style = textStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
