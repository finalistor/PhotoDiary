package com.photodiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.photodiary.navigation.AppNavGraph
import com.photodiary.ui.theme.PhotoDiaryTheme
import com.photodiary.ui.theme.ThemePreset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as PhotoDiaryApplication
        val repository = app.container.repository
        val userPreferences = app.container.userPreferences

        setContent {
            val themeMode by userPreferences.themeModeFlow.collectAsState(
                initial = com.photodiary.ui.theme.ThemeMode.SYSTEM
            )
            val themePreset by userPreferences.themePresetFlow.collectAsState(
                initial = ThemePreset.TERRACOTTA
            )
            PhotoDiaryTheme(themeMode = themeMode, themePreset = themePreset) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        repository = repository,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
}
