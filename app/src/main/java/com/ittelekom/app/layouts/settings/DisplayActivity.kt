package com.ittelekom.app.layouts.settings

import ThemeManager
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ittelekom.app.ui.theme.ITTelekomTheme

class DisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val selectedTheme = rememberSaveable { mutableStateOf("system") }
            val context = applicationContext

            // Загрузить тему из настроек
            val sharedPreferences =
                context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            selectedTheme.value = sharedPreferences.getString("theme", "system") ?: "system"

            ITTelekomTheme(window = window) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                DisplayScreen(
                    onBackPressed = { finish() },
                    onThemeChange = { newTheme ->
                        ThemeManager.saveTheme(applicationContext, newTheme)
                    }
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    onBackPressed: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val openDialogTheme = remember { mutableStateOf(false) }

    val themeNameMap = mapOf(
        "light" to "Светлая",
        "dark" to "Тёмная",
        "system" to "Системная"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отображение") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            if (openDialogTheme.value) {
                DialogTheme(
                    selectedTheme = ThemeManager.currentTheme.value,
                    onOptionSelected = { newTheme ->
                        onThemeChange(newTheme)
                        openDialogTheme.value = false
                    },
                    onDismissRequest = { openDialogTheme.value = false }
                )
            }

            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ListItem(
                        modifier = Modifier.height(40.dp),
                        headlineContent = {
                            Text(
                                text = "Тема",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { openDialogTheme.value = true }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ListItem(
                            headlineContent = { Text("Текущая тема") },
                            supportingContent = {
                                Text(themeNameMap[ThemeManager.currentTheme.value] ?: "Системная")
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTheme(
    selectedTheme: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val themeMap = mapOf(
        "light" to "Светлая",
        "dark" to "Тёмная",
        "system" to "Системная"
    )

    AlertDialog(
        text = {
            Column(Modifier.selectableGroup()) {
                themeMap.forEach { (key, value) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (key == selectedTheme),
                                onClick = { onOptionSelected(key) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (key == selectedTheme),
                            onClick = null
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Закрыть")
            }
        },
    )
}

