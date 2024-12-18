import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object ThemeManager {
    private const val PREFS_NAME = "app_preferences"
    private const val THEME_KEY = "theme"

    var currentTheme = mutableStateOf("system")

    fun loadTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentTheme.value = sharedPreferences.getString(THEME_KEY, "system") ?: "system"
    }

    fun saveTheme(context: Context, theme: String) {
        currentTheme.value = theme
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(THEME_KEY, theme).apply()
    }
}