package com.droidspec.presentation.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.droidspec.R
import com.droidspec.presentation.settings.ExportFormat
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.PrimaryButton

@Composable
fun ReportsScreen(
    busy: Boolean,
    text: String?,
    exportFormat: ExportFormat,
    savedMessage: String?,
    onGenerate: () -> Unit,
    onSave: () -> Unit,
    onConsumeSavedMessage: () -> Unit,
    onFormatChange: (ExportFormat) -> Unit
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var shareRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedMessage) {
        savedMessage?.let {
            snackbar.showSnackbar(context.getString(R.string.report_saved))
            onConsumeSavedMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.report_format),
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    TextButton(onClick = { onFormatChange(ExportFormat.TXT) }) {
                        Text(
                            stringResource(R.string.export_txt),
                            color = if (exportFormat == ExportFormat.TXT)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                    TextButton(onClick = { onFormatChange(ExportFormat.JSON) }) {
                        Text(
                            stringResource(R.string.export_json),
                            color = if (exportFormat == ExportFormat.JSON)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton(
                        onClick = onGenerate,
                        enabled = !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.report_generate)) }
                    OutlinedButton(
                        onClick = onSave,
                        enabled = text != null && !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.report_save)) }
                    OutlinedButton(
                        onClick = { shareRequested = true },
                        enabled = text != null && !busy,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.report_share)) }
                }
            }
        }

        if (busy) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.report_preview),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text ?: stringResource(R.string.report_empty),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }

    SnackbarHost(
        hostState = snackbar,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }

    LaunchedEffect(shareRequested) {
        val currentText = text
        if (shareRequested && currentText != null) {
            shareRequested = false
            val dir = java.io.File(context.filesDir, "reports").apply { mkdirs() }
            val file = java.io.File(dir, "droidspec_share.txt")
            file.writeText(currentText)
            val uri = FileProvider.getUriForFile(
                context, "com.droidspec.fileprovider", file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, currentText.take(100_000))
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }
}
