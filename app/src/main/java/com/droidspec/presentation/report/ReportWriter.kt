package com.droidspec.presentation.report

import android.content.Context
import com.droidspec.domain.model.RootHeuristic
import com.droidspec.domain.report.DeviceReport
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Turns a [DeviceReport] into TXT/JSON. Labels are localized through
 * Android string resources, so reports follow the current app language.
 */
class ReportWriter(private val context: Context) {

    private val na: String get() = context.getString(com.droidspec.R.string.common_not_available)
    private fun s(res: Int): String = context.getString(res)
    private fun fmt(bytes: Long): String =
        android.text.format.Formatter.formatShortFileSize(context, bytes)

    private fun sectionDevice(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.label_manufacturer) to r.device.manufacturer,
        s(com.droidspec.R.string.label_brand) to r.device.brand,
        s(com.droidspec.R.string.label_model) to r.device.model,
        s(com.droidspec.R.string.label_device) to r.device.device,
        s(com.droidspec.R.string.label_product) to r.device.product,
        s(com.droidspec.R.string.label_board) to r.device.board,
        s(com.droidspec.R.string.label_hardware) to r.device.hardware,
        s(com.droidspec.R.string.label_bootloader) to r.device.bootloader,
        s(com.droidspec.R.string.label_baseband) to r.device.baseband
    )

    private fun sectionAndroid(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.label_android_version) to r.device.androidVersion,
        s(com.droidspec.R.string.label_api_level) to r.device.sdkInt?.toString(),
        s(com.droidspec.R.string.label_security_patch) to r.device.securityPatch,
        s(com.droidspec.R.string.label_build_id) to r.device.buildId,
        s(com.droidspec.R.string.label_build_fingerprint) to r.device.buildFingerprint,
        s(com.droidspec.R.string.label_kernel) to r.device.kernelVersion,
        s(com.droidspec.R.string.label_architecture) to r.device.architecture,
        s(com.droidspec.R.string.label_abis) to r.device.supportedAbis.joinToString(", ").ifBlank { null },
        s(com.droidspec.R.string.label_root_indicator) to when (r.device.rootIndicator.heuristic) {
            RootHeuristic.LIKELY_PRESENT -> s(com.droidspec.R.string.root_heuristic_present)
            RootHeuristic.NOT_DETECTED -> s(com.droidspec.R.string.root_heuristic_absent)
            RootHeuristic.UNKNOWN -> na
        }
    )

    private fun sectionCpu(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.cpu_name) to r.cpu.name,
        s(com.droidspec.R.string.cpu_cores) to r.cpu.coreCount.takeIf { it > 0 }?.toString(),
        s(com.droidspec.R.string.label_architecture) to r.cpu.architecture,
        s(com.droidspec.R.string.label_abis) to r.cpu.supportedAbis.joinToString(", ").ifBlank { null },
        s(com.droidspec.R.string.cpu_current_freq) to r.cpu.currentFreqMhz?.let {
            s(com.droidspec.R.string.mhz_format).format(it)
        },
        s(com.droidspec.R.string.cpu_min_freq) to r.cpu.minFreqMhz?.let {
            s(com.droidspec.R.string.mhz_format).format(it)
        },
        s(com.droidspec.R.string.cpu_max_freq) to r.cpu.maxFreqMhz?.let {
            s(com.droidspec.R.string.mhz_format).format(it)
        }
    )

    private fun sectionGpu(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.gpu_renderer) to r.gpu.renderer,
        s(com.droidspec.R.string.gpu_vendor) to r.gpu.vendor,
        s(com.droidspec.R.string.gpu_gles_version) to r.gpu.glesVersion,
        s(com.droidspec.R.string.gpu_vulkan) to r.gpu.vulkan,
        s(com.droidspec.R.string.gpu_extensions) to r.gpu.extensions.takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
    )

    private fun sectionRam(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.ram_total) to fmt(r.ram.totalBytes),
        s(com.droidspec.R.string.ram_used) to fmt(r.ram.usedBytes),
        s(com.droidspec.R.string.ram_available) to fmt(r.ram.availableBytes),
        s(com.droidspec.R.string.ram_usage) to s(com.droidspec.R.string.unit_percent).format(r.ram.usagePercent)
    )

    private fun sectionStorage(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.storage_total) to fmt(r.storage.totalBytes),
        s(com.droidspec.R.string.storage_used) to fmt(r.storage.usedBytes),
        s(com.droidspec.R.string.storage_free) to fmt(r.storage.freeBytes),
        s(com.droidspec.R.string.storage_usage) to s(com.droidspec.R.string.unit_percent).format(r.storage.usagePercent)
    )

    private fun sectionBattery(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.batt_percent) to r.battery.percent?.let {
            s(com.droidspec.R.string.unit_percent).format(it)
        },
        s(com.droidspec.R.string.batt_status) to r.battery.statusKey,
        s(com.droidspec.R.string.batt_source) to r.battery.sourceKey,
        s(com.droidspec.R.string.batt_temperature) to r.battery.temperatureCelsius?.let {
            "%.1f %s".format(it, s(com.droidspec.R.string.unit_degrees_c))
        },
        s(com.droidspec.R.string.batt_voltage) to r.battery.voltageMv?.let {
            "%d %s".format(it, s(com.droidspec.R.string.unit_mv))
        },
        s(com.droidspec.R.string.batt_current) to r.battery.currentMa?.let {
            "%d %s".format(it, s(com.droidspec.R.string.unit_ma))
        },
        s(com.droidspec.R.string.batt_technology) to r.battery.technology,
        s(com.droidspec.R.string.batt_health) to r.battery.healthKey,
        s(com.droidspec.R.string.batt_remaining) to r.battery.remainingMah?.let { "%d mAh".format(it) },
        s(com.droidspec.R.string.batt_capacity) to r.battery.capacityPercent?.let {
            s(com.droidspec.R.string.unit_percent).format(it)
        }
    )

    private fun sectionThermal(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.thermal_battery) to r.thermal.batteryCelsius?.let {
            "%.1f %s".format(it, s(com.droidspec.R.string.unit_degrees_c))
        },
        s(com.droidspec.R.string.thermal_ambient) to r.thermal.ambientCelsius?.let {
            "%.1f %s".format(it, s(com.droidspec.R.string.unit_degrees_c))
        },
        s(com.droidspec.R.string.thermal_severity) to r.thermal.severity.name
    ) + r.thermal.zones.map {
        "${s(com.droidspec.R.string.thermal_zone_type)}: ${it.name} (${it.type ?: na})" to
            "%.1f %s".format(it.temperatureCelsius, s(com.droidspec.R.string.unit_degrees_c))
    }

    private fun sectionDisplay(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.disp_resolution) to r.display.widthPx?.let { "${it}x${r.display.heightPx}" },
        s(com.droidspec.R.string.disp_dpi) to r.display.densityDpi?.toString(),
        s(com.droidspec.R.string.disp_refresh) to r.display.refreshRateHz?.let {
            s(com.droidspec.R.string.unit_fps_hz).format(it)
        },
        s(com.droidspec.R.string.disp_hdr) to r.display.hdrTypes.takeIf { it.isNotEmpty() }
            ?.joinToString(", "),
        s(com.droidspec.R.string.disp_size) to r.display.physicalSizeInches?.let {
            s(com.droidspec.R.string.unit_inches).format(it)
        }
    )

    private fun sectionNetwork(r: DeviceReport): List<Pair<String, String?>> = listOf(
        s(com.droidspec.R.string.net_state) to r.network.connected?.toString(),
        s(com.droidspec.R.string.net_type) to r.network.transportKey,
        s(com.droidspec.R.string.net_downstream) to r.network.linkDownstreamKbps?.toString(),
        s(com.droidspec.R.string.net_metered) to r.network.metered?.toString(),
        s(com.droidspec.R.string.net_vpn_active) to r.network.vpnActive?.toString()
    )

    fun allSections(r: DeviceReport): List<Pair<String, List<Pair<String, String?>>>> = listOf(
        s(com.droidspec.R.string.report_section_device) to sectionDevice(r),
        s(com.droidspec.R.string.dev_section_android) to sectionAndroid(r),
        s(com.droidspec.R.string.report_section_cpu) to sectionCpu(r),
        s(com.droidspec.R.string.report_section_gpu) to sectionGpu(r),
        s(com.droidspec.R.string.report_section_ram) to sectionRam(r),
        s(com.droidspec.R.string.report_section_storage) to sectionStorage(r),
        s(com.droidspec.R.string.report_section_battery) to sectionBattery(r),
        s(com.droidspec.R.string.report_section_thermal) to sectionThermal(r),
        s(com.droidspec.R.string.report_section_display) to sectionDisplay(r),
        s(com.droidspec.R.string.report_section_sensors) to
            r.sensors.map { it.name to (it.vendor ?: na) },
        s(com.droidspec.R.string.report_section_network) to sectionNetwork(r)
    )

    fun toText(r: DeviceReport): String {
        val sb = StringBuilder()
        sb.appendLine("===== ${r.generatedBy} =====")
        sb.appendLine(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(r.generatedAtEpochMs)))
        sb.appendLine()
        allSections(r).forEach { (title, rows) ->
            sb.appendLine("--- $title ---")
            rows.forEach { (k, v) -> sb.appendLine("$k: ${v ?: na}") }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun toJson(r: DeviceReport): String {
        val root = JSONObject()
        root.put("generatedBy", r.generatedBy)
        root.put("generatedAt", r.generatedAtEpochMs)
        allSections(r).forEach { (title, rows) ->
            val obj = JSONObject()
            rows.forEach { (k, v) -> obj.put(k, v ?: JSONObject.NULL) }
            root.put(title, obj)
        }
        root.put(
            s(com.droidspec.R.string.report_section_sensors),
            JSONArray(r.sensors.map {
                JSONObject()
                    .put("name", it.name)
                    .put("vendor", it.vendor ?: JSONObject.NULL)
                    .put("version", it.version)
                    .put("powerMa", it.powerMa.toDouble())
                    .put("resolution", it.resolution.toDouble())
                    .put("maxRange", it.maxRange.toDouble())
            })
        )
        return root.toString(2)
    }

    fun saveToFile(content: String): String {
        val dir = File(context.filesDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "droidspec_report_$stamp.txt")
        file.writeText(content)
        return file.absolutePath
    }

    fun savedReportsDir(): File = File(context.filesDir, "reports")
}
