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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.core.model.ScreenState
import com.droidspec.domain.model.BatteryInfo
import com.droidspec.domain.model.DashboardData
import com.droidspec.domain.model.ThermalSeverity
import com.droidspec.ui.components.ErrorState
import com.droidspec.ui.components.GaugeBar
import com.droidspec.ui.components.LoadingState
import com.droidspec.ui.components.SpecCard
import android.text.format.Formatter
import androidx.compose.ui.platform.LocalContext

@Composable
fun DashboardScreen(state: ScreenState<DashboardData>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> DashboardContent(state.data)
    }
}

@Composable
private fun DashboardContent(data: DashboardData) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = data.device.model ?: stringResource(R.string.common_not_available),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(data.device.manufacturer, data.device.brand)
                        .distinct().joinToString(" · ")
                        .ifBlank { stringResource(R.string.common_not_available) },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (data.device.androidVersion != null && data.device.sdkInt != null) {
                        stringResource(R.string.dash_android_version, data.device.androidVersion, data.device.sdkInt)
                    } else stringResource(R.string.common_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpecCard(
                title = stringResource(R.string.dash_battery_card),
                modifier = Modifier.weight(1f)
            ) {
                BatteryMini(data.battery)
            }
            SpecCard(
                title = stringResource(R.string.dash_temperature_card),
                modifier = Modifier.weight(1f)
            ) {
                ThermalMini(data.thermal.batteryCelsius, data.thermal.severity)
            }
        }

        SpecCard(title = stringResource(R.string.dash_cpu_card)) {
            Text(
                text = data.cpu.name ?: stringResource(R.string.common_not_available),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoOrNa(stringResource(R.string.cpu_cores), data.cpu.coreCount.takeIf { it > 0 }?.toString())
            InfoOrNa(
                stringResource(R.string.cpu_usage),
                data.cpu.usagePercent?.let { stringResource(R.string.unit_percent, it.toInt()) }
            )
        }

        SpecCard(title = stringResource(R.string.dash_ram_card)) {
            GaugeBar(stringResource(R.string.ram_usage), data.ram.usagePercent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${Formatter.formatShortFileSize(context, data.ram.usedBytes)} / " +
                    Formatter.formatShortFileSize(context, data.ram.totalBytes),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SpecCard(title = stringResource(R.string.dash_storage_card)) {
            GaugeBar(stringResource(R.string.storage_usage), data.storage.usagePercent)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${Formatter.formatShortFileSize(context, data.storage.usedBytes)} / " +
                    Formatter.formatShortFileSize(context, data.storage.totalBytes),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        SpecCard(title = stringResource(R.string.dash_health_card)) {
            val warning = data.thermal.severity.ordinal >= ThermalSeverity.HOT.ordinal ||
                data.ram.lowMemory == true || data.battery.percent?.let { it <= 15 } == true
            Text(
                text = stringResource(if (warning) R.string.dash_health_warning else R.string.dash_health_good),
                style = MaterialTheme.typography.bodyLarge,
                color = if (warning) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BatteryMini(battery: BatteryInfo) {
    Text(
        text = battery.percent?.let { stringResource(R.string.unit_percent, it) }
            ?: stringResource(R.string.common_not_available),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    val stateText = when {
        battery.charging == true -> stringResource(R.string.dash_charging)
        battery.charging == false -> stringResource(R.string.dash_discharging)
        battery.statusKey == "full" -> stringResource(R.string.dash_full)
        else -> stringResource(R.string.dash_battery_state_unknown)
    }
    Text(stateText, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ThermalMini(batteryCelsius: Float?, severity: ThermalSeverity) {
    Text(
        text = batteryCelsius?.let { "%.1f".format(it) + stringResource(R.string.unit_degrees_c) }
            ?: stringResource(R.string.common_not_available),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(
            when (severity) {
                ThermalSeverity.NORMAL -> R.string.severity_normal
                ThermalSeverity.WARM -> R.string.severity_warm
                ThermalSeverity.HOT -> R.string.severity_hot
                ThermalSeverity.CRITICAL -> R.string.severity_critical
            }
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = when (severity) {
            ThermalSeverity.NORMAL -> MaterialTheme.colorScheme.primary
            ThermalSeverity.WARM -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        }
    )
}

@Composable
private fun InfoOrNa(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value ?: stringResource(R.string.common_not_available),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
