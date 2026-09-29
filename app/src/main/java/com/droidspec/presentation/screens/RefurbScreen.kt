package com.droidspec.presentation.screens

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.core.model.ScreenState
import com.droidspec.domain.model.RefurbReport
import com.droidspec.domain.model.RefurbStatus
import com.droidspec.ui.components.ErrorState
import com.droidspec.ui.components.LoadingState
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.PrimaryButton
import com.droidspec.ui.components.SpecCard

@Composable
fun RefurbScreen(state: ScreenState<RefurbReport>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> RefurbContent(state.data)
    }
}

@Composable
private fun RefurbContent(report: RefurbReport) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MatchHeader(report)
        SpecCard(stringResource(R.string.refurb_title)) {
            report.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = componentLabel(item.componentKey),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    VerdictChip(item.status)
                }
                item.readout?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                item.noteKey?.let { key ->
                    Text(
                        text = noteLabel(key),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        FingerprintSpeedCard()
        NoteText(stringResource(R.string.refurb_disclaimer))
    }
}

@Composable
private fun MatchHeader(report: RefurbReport) {
    val percent = report.matchPercent
    val color = when {
        percent == null -> MaterialTheme.colorScheme.tertiary
        percent >= 80 -> MaterialTheme.colorScheme.primary
        percent >= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    val headline = when {
        percent != null -> stringResource(R.string.refurb_match_percent, percent)
        else -> stringResource(R.string.refurb_no_reference_device)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
                    drawArc(color.copy(alpha = 0.22f), -90f, 360f, false, style = stroke)
                    val sweep = ((percent ?: report.trustScore) / 100f) * 360f
                    drawArc(color, -90f, sweep, false, style = stroke)
                }
                Text(
                    text = (percent ?: report.trustScore).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = stringResource(
                        R.string.refurb_counts,
                        report.matchedCount, report.mismatchedCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VerdictChip(status: RefurbStatus) {
    val (label, color) = when (status) {
        RefurbStatus.MATCHED ->
            stringResource(R.string.status_matched) to MaterialTheme.colorScheme.primary
        RefurbStatus.MISMATCHED ->
            stringResource(R.string.status_mismatched) to MaterialTheme.colorScheme.error
        RefurbStatus.UNREADABLE ->
            stringResource(R.string.status_unreadable) to MaterialTheme.colorScheme.outline
        RefurbStatus.NO_REFERENCE ->
            stringResource(R.string.status_no_reference) to MaterialTheme.colorScheme.tertiary
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Real fingerprint sensor responsiveness measurement (API 28+). */
@Composable
private fun FingerprintSpeedCard() {
    var ms by remember { mutableStateOf<Long?>(null) }
    var running by remember { mutableStateOf(false) }
    val context = LocalContext.current

    SpecCard(stringResource(R.string.refurb_fp_speed)) {
        Text(
            stringResource(R.string.refurb_fp_speed_desc),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = {
                    if (Build.VERSION.SDK_INT < 28) return@PrimaryButton
                    running = true
                    val activity = context as? androidx.fragment.app.FragmentActivity ?: return@PrimaryButton
                    val start = System.currentTimeMillis()
                    val executor = ContextCompatExecutor(activity)
                    val callback =
                        object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(
                                result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult
                            ) {
                                ms = System.currentTimeMillis() - start
                                running = false
                            }

                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                running = false
                            }
                        }
                    val info = android.hardware.biometrics.BiometricPrompt.PromptInfo.Builder()
                        .setTitle("DroidSpec")
                        .setSubtitle(context.getString(R.string.refurb_fp_speed_desc))
                        .setDeviceCredentialAllowed(false)
                        .setNegativeButtonText(context.getString(R.string.common_cancel))
                        .build()
                    val prompt = android.hardware.biometrics.BiometricPrompt.Builder(activity)
                        .setTitle("DroidSpec")
                        .setSubtitle(context.getString(R.string.refurb_fp_speed_desc))
                        .setDeviceCredentialAllowed(false)
                        .setNegativeButtonText(
                            context.getString(R.string.common_cancel),
                            executor
                        ) { _, _ -> running = false }
                        .build()
                    runCatching { prompt.authenticate(info, executor, callback) }
                },
                enabled = !running && Build.VERSION.SDK_INT >= 28
            ) { Text(stringResource(R.string.refurb_fp_speed_run)) }
            Spacer(Modifier.size(12.dp))
            ms?.let {
                Text(
                    text = stringResource(R.string.refurb_fp_speed_result, it),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } ?: if (running) Text(stringResource(R.string.common_loading)) else Unit
        }
    }
}

private class ContextCompatExecutor(context: android.content.Context) :
    java.util.concurrent.Executor {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    override fun execute(command: Runnable) {
        handler.post(command)
    }
}

@Composable
private fun componentLabel(key: String): String = stringResource(
    when (key) {
        "panel" -> R.string.refurb_panel
        "touch" -> R.string.refurb_touch
        "display_size" -> R.string.refurb_display_size
        "battery" -> R.string.refurb_battery
        "usb" -> R.string.refurb_usb
        "charger" -> R.string.refurb_charger
        "camera" -> R.string.refurb_camera
        "fingerprint" -> R.string.refurb_fingerprint
        "nfc" -> R.string.net_nfc
        "microphone" -> R.string.refurb_microphone
        "speaker" -> R.string.refurb_speaker
        "vibrator" -> R.string.refurb_vibrator
        else -> R.string.common_unknown
    }
)

@Composable
private fun noteLabel(key: String): String = stringResource(
    when (key) {
        "panel_vendor" -> R.string.note_panel_vendor
        "touch_driver" -> R.string.note_touch_driver
        "battery_wear" -> R.string.note_battery_wear
        "camera_count" -> R.string.note_camera_count
        "fp_missing" -> R.string.note_fp_missing
        "nfc_missing" -> R.string.note_nfc_missing
        else -> R.string.common_unknown
    }
)
