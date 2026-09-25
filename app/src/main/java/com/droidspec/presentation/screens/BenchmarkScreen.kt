package com.droidspec.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.domain.benchmark.BenchmarkEngine
import com.droidspec.ui.components.InfoRow
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.SpecCard
import com.droidspec.ui.components.PrimaryButton

@Composable
fun BenchmarkScreen(
    state: BenchmarkEngine.BenchState,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    var showWarning by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SpecCard(stringResource(R.string.bench_title)) {
            Text(
                stringResource(R.string.bench_disclaimer),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        when (state) {
            is BenchmarkEngine.BenchState.Idle -> {
                PrimaryButton(onClick = { showWarning = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_start))
                }
            }
            is BenchmarkEngine.BenchState.Running -> {
                val stageName = stringResource(
                    when (state.stage) {
                        BenchmarkEngine.Stage.CPU -> R.string.bench_stage_cpu
                        BenchmarkEngine.Stage.MEMORY -> R.string.bench_stage_memory
                        BenchmarkEngine.Stage.STORAGE -> R.string.bench_stage_storage
                    }
                )
                SpecCard(stageName) {
                    LinearProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.bench_running, state.progressPercent),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
            is BenchmarkEngine.BenchState.Done -> {
                SpecCard(stringResource(R.string.bench_result_score)) {
                    Text(
                        state.result.internalScore.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    NoteText(
                        stringResource(R.string.bench_done, state.result.durationMs / 1000f)
                    )
                }
                SpecCard(stringResource(R.string.bench_title)) {
                    InfoRow(
                        stringResource(R.string.bench_result_ops),
                        "%,d".format(state.result.cpuOpsPerSec)
                    )
                    InfoRow(
                        stringResource(R.string.bench_result_mem),
                        "%.0f".format(state.result.memoryMbps)
                    )
                    InfoRow(
                        stringResource(R.string.bench_result_storage),
                        "%.0f".format(state.result.storageMbps)
                    )
                }
                PrimaryButton(onClick = { showWarning = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_start))
                }
            }
            is BenchmarkEngine.BenchState.Cancelled -> {
                NoteText(stringResource(R.string.bench_cancelled))
                PrimaryButton(onClick = { showWarning = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_start))
                }
            }
        }
    }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text(stringResource(R.string.bench_warning_title)) },
            text = { Text(stringResource(R.string.bench_warning_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWarning = false
                        onStart()
                    }
                ) { Text(stringResource(R.string.common_start)) }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
