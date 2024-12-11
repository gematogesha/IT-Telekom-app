package com.ittelekom.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomLoadingIndicator(
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = color
        )
    }
}

@Composable
fun ButtonLoadingIndicator(
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp
) {
    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = color,
        strokeWidth = strokeWidth,
    )
}