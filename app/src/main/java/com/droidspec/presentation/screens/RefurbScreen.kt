package com.droidspec.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
        ScoreHeader(report)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountChip(
                label = stringResource(R.string.refurb_suspicious_count, report.suspiciousCount),
                color = MaterialTheme.colorScheme.error
            )
            CountChip(
                label = stringResource(R.string.refurb_unknown_count, report.unknownCount),
                color = MaterialTheme.colorScheme.tertiary
            )
        }

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
                    StatusChip(item.status)
                }
                item.detail?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        NoteText(stringResource(R.string.refurb_disclaimer))
    }
}

@Composable
private fun ScoreHeader(report: RefurbReport) {
    val (color, bandRes) = when {
        report.score >= 80 -> MaterialTheme.colorScheme.primary to R.string.refurb_risk_low
        report.score >= 40 -> MaterialTheme.colorScheme.tertiary to R.string.refurb_risk_med
        else -> MaterialTheme.colorScheme.error to R.string.refurb_risk_high
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(116.dp)) {
                    val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)
                    drawArc(
                        color = color.copy(alpha = 0.22f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * (report.score / 100f),
                        useCenter = false, style = stroke
                    )
                }
                Text(
                    text = report.score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.refurb_score),
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
                Text(
                    text = stringResource(bandRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = stringResource(
                        R.string.refurb_items_summary, report.items.size,
                        report.suspiciousCount + report.unknownCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CountChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f))) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun StatusChip(status: RefurbStatus) {
    val (label, color) = when (status) {
        RefurbStatus.GENUINE_LIKELY ->
            stringResource(R.string.status_ok) to MaterialTheme.colorScheme.primary
        RefurbStatus.SUSPICIOUS ->
            stringResource(R.string.status_suspicious) to MaterialTheme.colorScheme.error
        RefurbStatus.UNKNOWN ->
            stringResource(R.string.status_unknown) to MaterialTheme.colorScheme.tertiary
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

@Composable
private fun componentLabel(key: String): String = stringResource(
    when (key) {
        "touch" -> R.string.refurb_touch
        "display" -> R.string.refurb_display
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
