package com.droidspec.presentation.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "droidspec_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LanguageMode { SYSTEM, ENGLISH, ARABIC }
enum class ExportFormat { TXT, JSON }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val language: LanguageMode = LanguageMode.SYSTEM,
    val refreshMs: Long = 5000L,
    val exportFormat: ExportFormat = ExportFormat.TXT
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")
        val REFRESH_MS = longPreferencesKey("refresh_ms")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            theme = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            language = p[Keys.LANGUAGE]?.let { runCatching { LanguageMode.valueOf(it) }.getOrNull() }
                ?: LanguageMode.SYSTEM,
            refreshMs = p[Keys.REFRESH_MS] ?: 5000L,
            exportFormat = p[Keys.EXPORT_FORMAT]
                ?.let { runCatching { ExportFormat.valueOf(it) }.getOrNull() }
                ?: ExportFormat.TXT
        )
    }

    suspend fun setTheme(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setLanguage(mode: LanguageMode) =
        context.dataStore.edit { it[Keys.LANGUAGE] = mode.name }

    suspend fun setRefreshMs(ms: Long) =
        context.dataStore.edit { it[Keys.REFRESH_MS] = ms }

    suspend fun setExportFormat(format: ExportFormat) =
        context.dataStore.edit { it[Keys.EXPORT_FORMAT] = format.name }
}
