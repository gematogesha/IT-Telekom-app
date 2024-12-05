package com.ittelekom.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ittelekom.app.R

val FiraSans = FontFamily(
    Font(R.font.fira_sans, FontWeight.W400), // Замените my_custom_font на ваше имя файла
    Font(R.font.fira_sans_medium, FontWeight.W500) // Опционально: добавьте разные веса шрифта
)

// Set of Material typography styles to start with
val Typography = Typography(
    // Основной текст (обычный)
    bodyMedium = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp, // Лучше немного увеличить для читаемости
        letterSpacing = 0.sp
    ),
    // Крупный текст для основного контента
    bodyLarge = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // Мелкий текст
    bodySmall = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // Средний заголовок
    titleMedium = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Крупный заголовок
    titleLarge = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Мелкие подписи или кнопки
    labelSmall = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // Очень крупные заголовки для акцентов
    headlineLarge = TextStyle(
        fontFamily = FiraSans,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
)