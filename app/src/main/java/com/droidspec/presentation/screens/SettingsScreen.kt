package com.droidspec.presentation.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.presentation.settings.AppSettings
import com.droidspec.presentation.settings.ExportFormat
import com.droidspec.presentation.settings.LanguageMode
import com.droidspec.presentation.settings.ThemeMode
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.SpecCard

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onTheme: (ThemeMode) -> Unit,
    onLanguage: (LanguageMode) -> Unit,
    onRefreshMs: (Long) -> Unit,
    onExportFormat: (ExportFormat) -> Unit
) {
    val context = LocalContext.current
    val version = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: stringResource(R.string.common_unknown)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SpecCard(stringResource(R.string.settings_appearance)) {
            RadioOption(stringResource(R.string.theme_system), settings.theme == ThemeMode.SYSTEM) {
                onTheme(ThemeMode.SYSTEM)
            }
            RadioOption(stringResource(R.string.theme_light), settings.theme == ThemeMode.LIGHT) {
                onTheme(ThemeMode.LIGHT)
            }
            RadioOption(stringResource(R.string.theme_dark), settings.theme == ThemeMode.DARK) {
                onTheme(ThemeMode.DARK)
            }
        }

        SpecCard(stringResource(R.string.settings_language)) {
            RadioOption(stringResource(R.string.lang_system), settings.language == LanguageMode.SYSTEM) {
                onLanguage(LanguageMode.SYSTEM)
            }
            RadioOption(stringResource(R.string.lang_english), settings.language == LanguageMode.ENGLISH) {
                onLanguage(LanguageMode.ENGLISH)
            }
            RadioOption(stringResource(R.string.lang_arabic), settings.language == LanguageMode.ARABIC) {
                onLanguage(LanguageMode.ARABIC)
            }
        }

        SpecCard(stringResource(R.string.settings_monitoring)) {
            Text(
                stringResource(R.string.settings_refresh_interval),
                style = MaterialTheme.typography.bodyLarge
            )
            val options = listOf(
                1_000L to R.string.refresh_1s,
                2_000L to R.string.refresh_2s,
                5_000L to R.string.refresh_5s,
                10_000L to R.string.refresh_10s
            )
            options.forEach { (ms, labelRes) ->
                RadioOption(stringResource(labelRes), settings.refreshMs == ms) { onRefreshMs(ms) }
            }
        }

        SpecCard(stringResource(R.string.settings_reports)) {
            RadioOption(stringResource(R.string.export_txt), settings.exportFormat == ExportFormat.TXT) {
                onExportFormat(ExportFormat.TXT)
            }
            RadioOption(stringResource(R.string.export_json), settings.exportFormat == ExportFormat.JSON) {
                onExportFormat(ExportFormat.JSON)
            }
        }

        SpecCard(stringResource(R.string.settings_privacy)) {
            Text(stringResource(R.string.privacy_text), style = MaterialTheme.typography.bodyMedium)
        }

        SpecCard(stringResource(R.string.settings_permissions)) {
            Text(stringResource(R.string.permissions_text), style = MaterialTheme.typography.bodyMedium)
        }

        SpecCard(stringResource(R.string.settings_about)) {
            val uriHandler = LocalUriHandler.current
            val supportUrl = stringResource(R.string.about_github_url)
            Text(
                stringResource(R.string.about_version, version),
                style = MaterialTheme.typography.bodyLarge
            )
            val pkgInfo = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()
            val dateFmt = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
            pkgInfo?.let {
                Text(
                    stringResource(R.string.about_installed) + ": " + dateFmt.format(java.util.Date(it.firstInstallTime)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.about_updated) + ": " + dateFmt.format(java.util.Date(it.lastUpdateTime)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                stringResource(R.string.about_developer) + ": " +
                    stringResource(R.string.about_developer_value),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.about_support) + ": " + supportUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { uriHandler.openUri(supportUrl) }
            )
            Text(
                stringResource(R.string.about_licenses),
                style = MaterialTheme.typography.bodyMedium
            )
            NoteText(stringResource(R.string.licenses_text))
        }
    }
}

@Composable
private fun RadioOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}
