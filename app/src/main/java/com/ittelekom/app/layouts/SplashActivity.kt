package com.ittelekom.app.layouts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.ittelekom.app.R

//TODO: Сделать Splash Screen

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Показ кастомного Splash Screen
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), // Цвет фона
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Логотип"
                )
            }
        }

        // Переход на следующую активность после задержки
        window.decorView.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1500) // Задержка в 1.5 секунды
    }
}