package com.ittelekom.app.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ittelekom.app.screens.settings.DisplayActivity
import com.ittelekom.app.screens.settings.InfoActivity
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.ui.util.SetSystemBarsColor

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                SettingsScreen(onBackPressed = { finish() })
            }
        }
    }
}

data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val activityClass: Class<*>
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {

    SetSystemBarsColor()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current // Получаем контекст приложения

    val menuItems = listOf(
        MenuItem(
            title = "Отображение",
            subtitle = "Secondary text",
            icon = Icons.Outlined.Palette,
            activityClass = DisplayActivity::class.java
        ),
        MenuItem(
            title = "Информация",
            subtitle = "Application details",
            icon = Icons.Outlined.Info,
            activityClass = InfoActivity::class.java
        )
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Настройки", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = { innerPadding ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(menuItems) { menuItem ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        val intent = Intent(context, menuItem.activityClass)
                                        context.startActivity(intent)
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ListItem(
                                headlineContent = { Text(menuItem.title) },
                                supportingContent = { Text(menuItem.subtitle) },
                                leadingContent = {
                                    Icon(
                                        menuItem.icon,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
