package com.droidspec.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.core.model.ScreenState
import com.droidspec.domain.model.SensorSpec
import com.droidspec.ui.components.ErrorState
import com.droidspec.ui.components.InfoRow
import com.droidspec.ui.components.LoadingState
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.SpecCard

@Composable
fun SensorsScreen(state: ScreenState<List<SensorSpec>>, onSensorClick: (Int) -> Unit) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val sensors = state.data
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = stringResource(R.string.sensor_available_count, sensors.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (sensors.isEmpty()) {
                    NoteText(stringResource(R.string.sensor_no_sensors))
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    items(sensors, key = { "${it.type}-${it.name}-${it.vendor}-${it.hashCode()}" }) { sensor ->
                        SensorRow(sensor) { onSensorClick(sensor.type) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorRow(sensor: SensorSpec, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sensor.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    sensor.vendor ?: stringResource(R.string.common_not_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SensorDetailScreen(
    sensor: SensorSpec?,
    liveValues: FloatArray?
) {
    if (sensor == null) {
        LoadingState()
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SpecCard(sensor.name) {
            InfoRow(stringResource(R.string.sensor_vendor), sensor.vendor)
            InfoRow(stringResource(R.string.sensor_version), sensor.version.toString())
            InfoRow(
                stringResource(R.string.sensor_power),
                stringResource(R.string.unit_power_ma, sensor.powerMa)
            )
            InfoRow(stringResource(R.string.sensor_resolution), sensor.resolution.toString())
            InfoRow(stringResource(R.string.sensor_range), sensor.maxRange.toString())
            InfoRow(
                stringResource(R.string.sensor_wakeup),
                stringResource(if (sensor.wakeUp) R.string.net_yes else R.string.net_no)
            )
        }
        SpecCard(stringResource(R.string.sensor_live)) {
            val unit = sensor?.let { com.droidspec.domain.policy.SensorUnits.unitFor(it.type) }
            Text(
                text = when {
                    liveValues == null -> stringResource(R.string.common_not_available)
                    else -> liveValues.joinToString("   ") { "%.3f".format(it) } +
                        (unit?.let { "  $it" } ?: "")
                },
                style = MaterialTheme.typography.headlineSmall
            )
            NoteText(stringResource(R.string.sensor_stop_hint))
        }
    }
}
