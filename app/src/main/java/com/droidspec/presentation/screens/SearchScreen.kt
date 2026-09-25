package com.droidspec.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.domain.search.SearchEntry

@Composable
fun SearchScreen(
    query: String,
    results: List<SearchEntry>,
    searching: Boolean,
    onQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            singleLine = true
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (query.trim().length < 2 && !searching) {
                item {
                    Text(
                        stringResource(R.string.search_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (results.isEmpty() && !searching) {
                item {
                    Text(
                        stringResource(R.string.search_no_results),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(results.withIndex().toList(), key = { iv -> "${iv.index}-${iv.value.sectionKey}-${iv.value.title}" }) { iv ->
                val entry = iv.value
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = sectionLabel(entry.sectionKey),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(entry.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            entry.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun sectionLabel(key: String): String = stringResource(
    when (key) {
        "cpu" -> R.string.nav_cpu
        "gpu" -> R.string.nav_gpu
        "memory" -> R.string.nav_memory
        "storage" -> R.string.nav_storage
        "battery" -> R.string.nav_battery
        "display" -> R.string.nav_display
        "camera" -> R.string.nav_camera
        "sensors" -> R.string.nav_sensors
        "network" -> R.string.nav_network
        else -> R.string.nav_device
    }
)
