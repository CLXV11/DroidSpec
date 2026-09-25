package com.droidspec.presentation.screens

import android.text.format.Formatter
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.core.model.ScreenState
import com.droidspec.domain.model.BatteryInfo
import com.droidspec.domain.model.CameraSummary
import com.droidspec.domain.model.CpuInfo
import com.droidspec.domain.model.DeviceInfo
import com.droidspec.domain.model.DisplayInfo
import com.droidspec.domain.model.GpuInfo
import com.droidspec.domain.model.NetworkInfo
import com.droidspec.domain.model.RamInfo
import com.droidspec.domain.model.RootHeuristic
import com.droidspec.domain.model.StorageDetails
import com.droidspec.domain.model.StorageInfo
import com.droidspec.domain.model.ThermalSeverity
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.policy.CpuBrand
import com.droidspec.domain.policy.CpuBrandDetector
import com.droidspec.domain.policy.CpuModelResolver
import com.droidspec.ui.components.CpuBrandBadge
import com.droidspec.ui.components.CpuChipVisual
import com.droidspec.ui.components.ErrorState
import com.droidspec.ui.components.GaugeBar
import com.droidspec.ui.components.InfoRow
import com.droidspec.ui.components.LoadingState
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.SpecCard

// ------------------------------- Device -----------------------------------

@Composable
fun DeviceScreen(state: ScreenState<DeviceInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val d = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.dev_section_device)) {
                    InfoRow(stringResource(R.string.label_manufacturer), d.manufacturer)
                    InfoRow(stringResource(R.string.label_brand), d.brand)
                    InfoRow(stringResource(R.string.label_model), d.model)
                    InfoRow(stringResource(R.string.label_device), d.device)
                    InfoRow(stringResource(R.string.label_product), d.product)
                    InfoRow(stringResource(R.string.label_board), d.board)
                    InfoRow(stringResource(R.string.label_hardware), d.hardware)
                    InfoRow(stringResource(R.string.label_bootloader), d.bootloader)
                    InfoRow(stringResource(R.string.label_baseband), d.baseband)
                }
                SpecCard(stringResource(R.string.dev_section_android)) {
                    InfoRow(stringResource(R.string.label_android_version), d.androidVersion)
                    InfoRow(stringResource(R.string.label_api_level), d.sdkInt?.toString())
                    InfoRow(stringResource(R.string.label_security_patch), d.securityPatch)
                    InfoRow(stringResource(R.string.label_build_id), d.buildId)
                    InfoRow(stringResource(R.string.label_build_fingerprint), d.buildFingerprint)
                    InfoRow(stringResource(R.string.label_kernel), d.kernelVersion)
                    InfoRow(stringResource(R.string.label_architecture), d.architecture)
                    InfoRow(
                        stringResource(R.string.label_abis),
                        d.supportedAbis.joinToString(", ").ifBlank { null }
                    )
                    val uptime = d.uptimeMs?.let { com.droidspec.core.util.CalcUtils.splitDuration(it) }
                    InfoRow(
                        stringResource(R.string.label_uptime),
                        uptime?.let { (dd, hh, mm) ->
                            stringResource(R.string.uptime_format, dd, hh, mm)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.label_deep_sleep),
                        d.deepSleepPercent?.let { stringResource(R.string.unit_percent, it) }
                    )
                    InfoRow(
                        stringResource(R.string.label_release_year),
                        d.releaseYear?.let {
                            stringResource(R.string.release_year_value, it)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.label_build_date),
                        d.buildDateMs?.let {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                .format(java.util.Date(it))
                        }
                    )
                    InfoRow(stringResource(R.string.dev_selinux), d.selinuxStatus)
                    InfoRow(stringResource(R.string.dev_boots), d.bootCount?.toString())
                    InfoRow(
                        stringResource(R.string.dev_interactive),
                        d.interactive?.let {
                            stringResource(if (it) R.string.dev_screen_on else R.string.dev_screen_off)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.dev_idle),
                        d.idleMode?.let {
                            stringResource(if (it) R.string.common_enabled else R.string.common_disabled)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.label_root_indicator),
                        when (d.rootIndicator.heuristic) {
                            RootHeuristic.LIKELY_PRESENT -> stringResource(R.string.root_heuristic_present)
                            RootHeuristic.NOT_DETECTED -> stringResource(R.string.root_heuristic_absent)
                            RootHeuristic.UNKNOWN -> null
                        }
                    )
                }
            }
        }
    }
}

// ------------------------------- CPU --------------------------------------

@Composable
fun CpuScreen(state: ScreenState<CpuInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val c = state.data
            val brand = CpuBrandDetector.detect(c.name)
            val marketingName = CpuModelResolver.resolve(c.name)
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (brand != CpuBrand.GENERIC) {
                    SpecCard(title = stringResource(R.string.nav_cpu)) {
                        CpuBrandBadge(brand = brand)
                    }
                }
                SpecCard(title = stringResource(R.string.nav_cpu)) {
                    CpuChipVisual(
                        brand = brand,
                        cpuName = marketingName ?: c.name,
                        coreCount = c.coreCount,
                        maxFreqMhz = c.maxFreqMhz
                    )
                }
                SpecCard(stringResource(R.string.section_title_processor)) {
                    InfoRow(stringResource(R.string.cpu_name), c.name)
                    InfoRow(stringResource(R.string.cpu_marketing), marketingName)
                    InfoRow(stringResource(R.string.cpu_cores), c.coreCount.takeIf { it > 0 }?.toString())
                    InfoRow(stringResource(R.string.label_architecture), c.architecture)
                    InfoRow(stringResource(R.string.label_abis), c.supportedAbis.joinToString(", ").ifBlank { null })
                    InfoRow(
                        stringResource(R.string.cpu_usage),
                        c.usagePercent?.let { stringResource(R.string.unit_percent, it.toInt()) }
                    )
                }
                SpecCard(stringResource(R.string.cpu_name)) {
                    InfoRow(
                        stringResource(R.string.cpu_current_freq),
                        c.currentFreqMhz?.let { stringResource(R.string.mhz_format, it) }
                    )
                    InfoRow(
                        stringResource(R.string.cpu_min_freq),
                        c.minFreqMhz?.let { stringResource(R.string.mhz_format, it) }
                    )
                    InfoRow(
                        stringResource(R.string.cpu_max_freq),
                        c.maxFreqMhz?.let { stringResource(R.string.mhz_format, it) }
                    )
                    if (c.currentFreqMhz == null && c.perCore.all { it.currentMhz == null }) {
                        NoteText(stringResource(R.string.cpu_freq_note))
                    }
                }
                if (c.perCore.any { it.currentMhz != null || it.maxMhz != null }) {
                    SpecCard(stringResource(R.string.cpu_per_core)) {
                        c.perCore.forEach { core ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "CPU${core.index}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = core.currentMhz?.let { stringResource(R.string.mhz_format, it) }
                                        ?: stringResource(R.string.common_not_available),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------- GPU --------------------------------------

@Composable
fun GpuScreen(state: ScreenState<GpuInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val g = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.section_title_graphics)) {
                    InfoRow(stringResource(R.string.gpu_renderer), g.renderer)
                    InfoRow(stringResource(R.string.gpu_vendor), g.vendor)
                    InfoRow(stringResource(R.string.gpu_gles_version), g.glesVersion)
                    InfoRow(stringResource(R.string.gpu_vulkan), g.vulkan)
                    NoteText(stringResource(R.string.gpu_context_note))
                }
                if (g.extensions.isNotEmpty()) {
                    SpecCard(stringResource(R.string.gpu_extensions)) {
                        InfoRow(
                            stringResource(R.string.gpu_ext_count),
                            stringResource(R.string.gpu_ext_count_value, g.extensions.size)
                        )
                        Text(
                            text = g.extensions.take(40).joinToString("  ·  "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------- Memory -----------------------------------

@Composable
fun MemoryScreen(state: ScreenState<RamInfo>) {
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
                SpecCard(stringResource(R.string.section_title_memory)) {
                    GaugeBar(stringResource(R.string.ram_usage), m.usagePercent)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(stringResource(R.string.ram_total), Formatter.formatShortFileSize(context, m.totalBytes))
                    InfoRow(stringResource(R.string.ram_used), Formatter.formatShortFileSize(context, m.usedBytes))
                    InfoRow(stringResource(R.string.ram_available), Formatter.formatShortFileSize(context, m.availableBytes))
                }
                SpecCard(stringResource(R.string.ram_low_memory)) {
                    InfoRow(
                        stringResource(R.string.ram_low_memory),
                        m.lowMemory?.let {
                            stringResource(if (it) R.string.common_enabled else R.string.common_disabled)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.ram_threshold),
                        m.thresholdBytes?.let { Formatter.formatShortFileSize(context, it) }
                    )
                }
            }
        }
    }
}

// ------------------------------- Storage ----------------------------------

@Composable
fun StorageScreen(state: ScreenState<StorageDetails>) {
    val context = LocalContext.current
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val st = state.data.internalStorage
            val ext = state.data.external
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.nav_storage)) {
                    GaugeBar(stringResource(R.string.storage_usage), st.usagePercent)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(stringResource(R.string.storage_total), Formatter.formatShortFileSize(context, st.totalBytes))
                    InfoRow(stringResource(R.string.storage_used), Formatter.formatShortFileSize(context, st.usedBytes))
                    InfoRow(stringResource(R.string.storage_free), Formatter.formatShortFileSize(context, st.freeBytes))
                }
                SpecCard(stringResource(R.string.storage_external)) {
                    if (ext != null) {
                        InfoRow(
                            stringResource(R.string.storage_total),
                            Formatter.formatShortFileSize(context, ext.totalBytes)
                        )
                        InfoRow(
                            stringResource(R.string.storage_free),
                            Formatter.formatShortFileSize(context, ext.freeBytes)
                        )
                        InfoRow(
                            stringResource(R.string.storage_type),
                            stringResource(
                                if (ext.removable) R.string.storage_removable else R.string.storage_emulated
                            )
                        )
                    } else {
                        NoteText(stringResource(R.string.storage_no_external))
                    }
                }
                NoteText(stringResource(R.string.storage_note))
            }
        }
    }
}

// ------------------------------- Battery ----------------------------------

@Composable
fun BatteryScreen(state: ScreenState<BatteryInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val b = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.section_title_power)) {
                    Text(
                        text = b.percent?.let { stringResource(R.string.unit_percent, it) }
                            ?: stringResource(R.string.common_not_available),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        stringResource(R.string.batt_status),
                        b.statusKey?.let { batteryStatusText(it) }
                    )
                    InfoRow(
                        stringResource(R.string.batt_source),
                        b.sourceKey?.let { batterySourceText(it) }
                    )
                    InfoRow(
                        stringResource(R.string.batt_saver),
                        b.batterySaverOn?.let {
                            stringResource(if (it) R.string.common_enabled else R.string.common_disabled)
                        }
                    )
                }
                SpecCard(stringResource(R.string.nav_battery)) {
                    InfoRow(
                        stringResource(R.string.batt_temperature),
                        b.temperatureCelsius?.let {
                            "%.1f %s".format(it, stringResource(R.string.unit_degrees_c))
                        }
                    )
                    InfoRow(
                        stringResource(R.string.batt_voltage),
                        b.voltageMv?.let { "%d %s".format(it, stringResource(R.string.unit_mv)) }
                    )
                    InfoRow(
                        stringResource(R.string.batt_current),
                        b.currentMa?.let { "%d %s".format(it, stringResource(R.string.unit_ma)) }
                    )
                    val watts = b.voltageMv?.let { v ->
                        b.currentMa?.let { a ->
                            "%.1f W".format(kotlin.math.abs(v * a) / 1_000_000.0)
                        }
                    }
                    InfoRow(
                        stringResource(R.string.batt_power),
                        watts?.let { "$it (${stringResource(R.string.common_estimated_by_android)})" }
                    )
                    InfoRow(stringResource(R.string.batt_technology), b.technology)
                    InfoRow(
                        stringResource(R.string.batt_health),
                        b.healthKey?.let { batteryHealthText(it) }
                    )
                    InfoRow(
                        stringResource(R.string.batt_remaining),
                        b.remainingMah?.let {
                            "%d mAh (%s)".format(it, stringResource(R.string.common_estimated_by_android))
                        }
                    )
                    InfoRow(
                        stringResource(R.string.batt_capacity),
                        b.capacityPercent?.let {
                            stringResource(R.string.unit_percent, it) + " (" +
                                stringResource(R.string.common_estimated_by_android) + ")"
                        }
                    )
                    NoteText(stringResource(R.string.batt_health_note))
                }
            }
        }
    }
}

@Composable
fun batteryHealthText(key: String): String = stringResource(
    when (key) {
        "good" -> R.string.batt_health_good
        "overheat" -> R.string.batt_health_overheat
        "dead" -> R.string.batt_health_dead
        "over_voltage" -> R.string.batt_health_over_voltage
        "unspecified" -> R.string.batt_health_unspecified
        "cold" -> R.string.batt_health_cold
        else -> R.string.batt_health_unknown
    }
)

@Composable
fun batteryStatusText(key: String): String = stringResource(
    when (key) {
        "charging" -> R.string.batt_status_charging
        "discharging" -> R.string.batt_status_discharging
        "full" -> R.string.batt_status_full
        "not_charging" -> R.string.batt_status_not_charging
        else -> R.string.common_unknown
    }
)

@Composable
fun batterySourceText(key: String): String = stringResource(
    when (key) {
        "usb" -> R.string.batt_source_usb
        "ac" -> R.string.batt_source_ac
        "wireless" -> R.string.batt_source_wireless
        else -> R.string.batt_source_other
    }
)

// ------------------------------- Thermal ----------------------------------

@Composable
fun ThermalScreen(state: ScreenState<ThermalSnapshot>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val t = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.thermal_severity)) {
                    Text(
                        text = stringResource(
                            when (t.severity) {
                                ThermalSeverity.NORMAL -> R.string.severity_normal
                                ThermalSeverity.WARM -> R.string.severity_warm
                                ThermalSeverity.HOT -> R.string.severity_hot
                                ThermalSeverity.CRITICAL -> R.string.severity_critical
                            }
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (t.severity) {
                            ThermalSeverity.NORMAL -> MaterialTheme.colorScheme.primary
                            ThermalSeverity.WARM -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    NoteText(
                        stringResource(
                            R.string.thermal_thresholds,
                            t.thresholds.warmC, t.thresholds.hotC, t.thresholds.criticalC
                        )
                    )
                }
                SpecCard(stringResource(R.string.nav_thermal)) {
                    InfoRow(
                        stringResource(R.string.thermal_battery),
                        t.batteryCelsius?.let {
                            "%.1f %s".format(it, stringResource(R.string.unit_degrees_c))
                        }
                    )
                    InfoRow(
                        stringResource(R.string.thermal_ambient),
                        t.ambientCelsius?.let {
                            "%.1f %s".format(it, stringResource(R.string.unit_degrees_c))
                        }
                    )
                }
                if (t.zones.isNotEmpty()) {
                    SpecCard(stringResource(R.string.thermal_zones)) {
                        t.zones.forEach { zone ->
                            InfoRow(
                                "${zone.name} (${zone.type ?: stringResource(R.string.common_unknown)})",
                                "%.1f %s".format(zone.temperatureCelsius, stringResource(R.string.unit_degrees_c))
                            )
                        }
                    }
                }
                if (t.batteryCelsius == null && t.ambientCelsius == null && t.zones.isEmpty()) {
                    NoteText(stringResource(R.string.thermal_no_data))
                }
                NoteText(stringResource(R.string.thermal_note))
            }
        }
    }
}

// ------------------------------- Display ----------------------------------

@Composable
fun DisplayScreen(state: ScreenState<DisplayInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val d = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.nav_display)) {
                    InfoRow(
                        stringResource(R.string.disp_resolution),
                        d.widthPx?.let { "${d.widthPx} × ${d.heightPx}" }
                    )
                    InfoRow(stringResource(R.string.disp_density), d.density?.toString())
                    InfoRow(stringResource(R.string.disp_smallest), d.smallestWidthDp?.let { "%d dp".format(it) })
                    InfoRow(stringResource(R.string.disp_dpi), d.densityDpi?.toString())
                    InfoRow(stringResource(R.string.label_density_bucket), d.densityBucket)
                    InfoRow(
                        stringResource(R.string.disp_refresh),
                        d.refreshRateHz?.let { stringResource(R.string.unit_fps_hz, it) }
                    )
                    val uiMode = LocalContext.current.resources.configuration.uiMode
                    InfoRow(
                        stringResource(R.string.disp_dark),
                        ((uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                            android.content.res.Configuration.UI_MODE_NIGHT_YES).let {
                            stringResource(if (it) R.string.common_enabled else R.string.common_disabled)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.disp_font_scale),
                        LocalContext.current.resources.configuration.fontScale.toString()
                    )
                    InfoRow(
                        stringResource(R.string.disp_orientation),
                        stringResource(
                            when (d.orientationKey) {
                                "portrait" -> R.string.disp_orientation_portrait
                                "landscape" -> R.string.disp_orientation_landscape
                                "square" -> R.string.disp_orientation_square
                                else -> R.string.disp_orientation_undefined
                            }
                        )
                    )
                    InfoRow(
                        stringResource(R.string.disp_size),
                        d.physicalSizeInches?.let { stringResource(R.string.unit_inches, it) }
                    )
                }
                if (d.modes.isNotEmpty()) {
                    SpecCard(stringResource(R.string.disp_modes)) {
                        d.modes.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
                SpecCard(stringResource(R.string.disp_hdr)) {
                    Text(
                        text = d.hdrTypes.takeIf { it.isNotEmpty() }?.joinToString(" · ")
                            ?: stringResource(R.string.disp_hdr_none),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ------------------------------- Camera -----------------------------------

@Composable
fun CameraScreen(state: ScreenState<CameraSummary>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val c = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.nav_camera)) {
                    InfoRow(stringResource(R.string.cam_count), c.totalCount.takeIf { it > 0 }?.toString())
                }
                c.cameras.forEach { cam ->
                    SpecCard(
                        stringResource(
                            when (cam.facingKey) {
                                "front" -> R.string.cam_facing_front
                                "back" -> R.string.cam_facing_back
                                "external" -> R.string.cam_facing_external
                                else -> R.string.cam_facing_unknown
                            }
                        ) + " · ID ${cam.id}"
                    ) {
                        InfoRow(
                            stringResource(R.string.cam_resolution),
                            cam.megapixelsCalculated?.let { stringResource(R.string.megapixels_format, it) }
                        )
                        InfoRow(stringResource(R.string.cam_sensor_size), cam.sensorSizeMm)
                        InfoRow(
                            stringResource(R.string.cam_focal),
                            cam.focalLengthsMm.takeIf { it.isNotEmpty() }
                                ?.joinToString(", ") { "%.1f mm".format(it) }
                        )
                        InfoRow(
                            stringResource(R.string.cam_aperture),
                            cam.apertures.takeIf { it.isNotEmpty() }
                                ?.joinToString(", ") { "f/%.1f".format(it) }
                        )
                        InfoRow(
                            stringResource(R.string.cam_autofocus),
                            cam.autofocusSupported?.let {
                                stringResource(if (it) R.string.common_supported else R.string.common_not_supported)
                            }
                        )
                        InfoRow(
                            stringResource(R.string.cam_flash),
                            cam.flashSupported?.let {
                                stringResource(if (it) R.string.common_supported else R.string.common_not_supported)
                            }
                        )
                        if (cam.outputSizes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.cam_outputs),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            cam.outputSizes.forEach {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (cam.megapixelsCalculated != null) {
                            NoteText(stringResource(R.string.common_calculated))
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------- Network ----------------------------------

@Composable
fun NetworkScreen(state: ScreenState<NetworkInfo>) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error)
        state.data != null -> {
            val n = state.data
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecCard(stringResource(R.string.net_state)) {
                    InfoRow(
                        stringResource(R.string.net_state),
                        n.connected?.let {
                            stringResource(if (it) R.string.net_connected else R.string.net_disconnected)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.net_type),
                        n.transportKey?.let {
                            stringResource(
                                when (it) {
                                    "wifi" -> R.string.net_type_wifi
                                    "cellular" -> R.string.net_type_cellular
                                    "ethernet" -> R.string.net_type_ethernet
                                    "vpn" -> R.string.net_type_vpn
                                    else -> R.string.net_type_other
                                }
                            )
                        }
                    )
                    InfoRow(
                        stringResource(R.string.net_link_speed),
                        n.wifiLinkSpeedMbps?.let { stringResource(R.string.unit_mbps, it) }
                    )
                    InfoRow(
                        stringResource(R.string.net_downstream),
                        n.linkDownstreamKbps?.let { stringResource(R.string.unit_kbps, it) }
                    )
                    InfoRow(
                        stringResource(R.string.net_metered),
                        n.metered?.let {
                            stringResource(if (it) R.string.net_yes else R.string.net_no)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.net_vpn_active),
                        n.vpnActive?.let {
                            stringResource(if (it) R.string.net_yes else R.string.net_no)
                        }
                    )
                }
                SpecCard(stringResource(R.string.section_title_connectivity)) {
                    InfoRow(
                        stringResource(R.string.net_bluetooth),
                        when {
                            !n.bluetoothSupported -> stringResource(R.string.common_not_supported)
                            n.bluetoothEnabled != null -> stringResource(
                                if (n.bluetoothEnabled) R.string.common_enabled else R.string.common_disabled
                            )
                            else -> stringResource(R.string.common_supported)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.net_nfc),
                        when {
                            !n.nfcSupported -> stringResource(R.string.common_not_supported)
                            n.nfcEnabled != null -> stringResource(
                                if (n.nfcEnabled) R.string.common_enabled else R.string.common_disabled
                            )
                            else -> stringResource(R.string.common_supported)
                        }
                    )
                    InfoRow(
                        stringResource(R.string.net_gps),
                        stringResource(
                            if (n.gpsSupported) R.string.common_supported else R.string.common_not_supported
                        )
                    )
                    InfoRow(
                        stringResource(R.string.net_usb),
                        stringResource(
                            if (n.usbSupported) R.string.common_supported else R.string.common_not_supported
                        )
                    )
                    InfoRow(stringResource(R.string.net_usb_devices), n.usbDeviceCount?.toString())
                }
            }
        }
    }
}
