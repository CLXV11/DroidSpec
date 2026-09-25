package com.droidspec

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import com.droidspec.core.di.AppViewModelFactory
import com.droidspec.navigation.AppNavHost
import com.droidspec.presentation.settings.AppSettings
import com.droidspec.presentation.settings.LanguageMode
import com.droidspec.presentation.settings.ThemeMode
import com.droidspec.ui.theme.DroidSpecTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DroidSpecApp).container
        val factory = AppViewModelFactory(container)

        setContent {
            // null = DataStore hasn't emitted yet; never act on the placeholder
            val settings by container.settingsRepository.settings
                .collectAsState(initial = null)

            LaunchedEffect(settings?.language) {
                val language = settings?.language ?: return@LaunchedEffect
                val locales = when (language) {
                    LanguageMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
                    LanguageMode.ENGLISH -> LocaleListCompat.forLanguageTags("en")
                    LanguageMode.ARABIC -> LocaleListCompat.forLanguageTags("ar")
                }
                if (AppCompatDelegate.getApplicationLocales() != locales) {
                    AppCompatDelegate.setApplicationLocales(locales)
                }
            }

            DroidSpecTheme(themeMode = settings?.theme ?: ThemeMode.SYSTEM) {
                AppNavHost(container = container, factory = factory)
            }
        }
    }
}
