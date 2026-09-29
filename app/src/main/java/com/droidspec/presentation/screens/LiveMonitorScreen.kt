package com.droidspec.presentation.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.core.model.ScreenState
import com.droidspec.domain.model.LiveMetrics
import com.droidspec.ui.components.ErrorState
import com.droidspec.ui.components.GaugeBar
import com.droidspec.ui.components.InfoRow
import com.droidspec.ui.components.LoadingState
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.SpecCard

@Composable
fun LiveMonitorScreen(state: ScreenState<LiveMetrics>) {
    val context = LocalContext.current
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val m = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.live_title)) {
                    GaugeBar(
                        stringResource(R.string.cpu_usage),
                        m.cpuUsagePercent?.toInt() ?: 0
                    )
                    InfoRow(
                        stringResource(R.string.cpu_usage),
                        m.cpuUsagePercent?.let { stringResource(R.string.unit_percent, it.toInt()) }
                    )
                }
                SpecCard(stringResource(R.string.dash_ram_card)) {
                    GaugeBar(stringResource(R.string.ram_usage), m.ram.usagePercent)
                    InfoRow(
                        stringResource(R.string.ram_used),
                        "${Formatter.formatShortFileSize(context, m.ram.usedBytes)} / " +
                            Formatter.formatShortFileSize(context, m.ram.totalBytes)
                    )
                }
                SpecCard(stringResource(R.string.nav_storage)) {
                    GaugeBar(stringResource(R.string.storage_usage), m.storage.usagePercent)
                }
                SpecCard(stringResource(R.string.nav_battery)) {
                    InfoRow(
                        stringResource(R.string.batt_percent),
                        m.battery.percent?.let { stringResource(R.string.unit_percent, it) }
                    )
                    InfoRow(
                        stringResource(R.string.batt_temperature),
                        m.battery.temperatureCelsius?.let {
                            "%.1f %s".format(it, stringResource(R.string.unit_degrees_c))
                        }
                    )
                }
                SpecCard(stringResource(R.string.nav_thermal)) {
                    InfoRow(
                        stringResource(R.string.thermal_battery),
                        m.thermal.batteryCelsius?.let {
                            "%.1f %s".format(it, stringResource(R.string.unit_degrees_c))
                        }
                    )
                }
                NoteText(stringResource(R.string.live_note))
            }
        }
    }
}
