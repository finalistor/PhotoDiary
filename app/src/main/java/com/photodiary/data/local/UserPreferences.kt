package com.photodiary.data.local

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.photodiary.ui.theme.ThemeMode
import com.photodiary.ui.theme.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.SYSTEM }
    }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    val themePresetFlow: Flow<ThemePreset> = context.dataStore.data.map { prefs ->
        val name = prefs[THEME_PRESET_KEY] ?: ThemePreset.TERRACOTTA.name
        try { ThemePreset.valueOf(name) } catch (_: Exception) { ThemePreset.TERRACOTTA }
    }.distinctUntilChanged()

    suspend fun setThemePreset(preset: ThemePreset) {
        context.dataStore.edit { prefs ->
            prefs[THEME_PRESET_KEY] = preset.name
        }
    }

    val customPrimaryColorFlow: Flow<Color> = context.dataStore.data.map { prefs ->
        val hex = prefs[CUSTOM_PRIMARY_COLOR_KEY] ?: "FFFF6B8A"
        try {
            Color(java.lang.Long.parseLong(hex, 16).toInt())
        } catch (_: Exception) {
            Color(0xFFFF6B8A)
        }
    }.distinctUntilChanged()

    suspend fun setCustomPrimaryColor(color: Color) {
        context.dataStore.edit { prefs ->
            val hex = java.lang.Long.toHexString(color.value.toLong() and 0xFFFFFFFFL)
            prefs[CUSTOM_PRIMARY_COLOR_KEY] = hex.padStart(8, '0').uppercase()
        }
    }

    val customTagsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        parseJsonList(prefs[CUSTOM_TAGS_KEY])
    }.distinctUntilChanged()

    suspend fun addCustomTag(tag: String) {
        addCustomItem(CUSTOM_TAGS_KEY, tag)
    }

    suspend fun deleteCustomTag(tag: String) {
        deleteCustomItem(CUSTOM_TAGS_KEY, tag)
    }

    private suspend fun addCustomItem(key: Preferences.Key<String>, item: String) {
        context.dataStore.edit { prefs ->
            val current = parseJsonList(prefs[key]).toMutableList()
            if (item !in current) {
                current.add(item)
                prefs[key] = JSONArray(current).toString()
            }
        }
    }

    private suspend fun deleteCustomItem(key: Preferences.Key<String>, item: String) {
        context.dataStore.edit { prefs ->
            val current = parseJsonList(prefs[key]).toMutableList()
            current.remove(item)
            prefs[key] = JSONArray(current).toString()
        }
    }

    private fun parseJsonList(json: String?): List<String> {
        if (json == null) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) { emptyList() }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val THEME_PRESET_KEY = stringPreferencesKey("theme_preset")
        private val CUSTOM_PRIMARY_COLOR_KEY = stringPreferencesKey("custom_primary_color")
        private val CUSTOM_TAGS_KEY = stringPreferencesKey("custom_tags")
    }
}
