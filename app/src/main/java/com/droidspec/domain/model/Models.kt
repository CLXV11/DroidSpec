package com.droidspec.domain.model

// ---------- Device / Android system ----------
data class DeviceInfo(
    val manufacturer: String?,
    val brand: String?,
    val model: String?,
    val device: String?,
    val product: String?,
    val board: String?,
    val hardware: String?,
    val bootloader: String?,
    val baseband: String?,
    val androidVersion: String?,
    val sdkInt: Int?,
    val securityPatch: String?,
    val buildId: String?,
    val buildFingerprint: String?,
    val kernelVersion: String?,
    val architecture: String?,
    val supportedAbis: List<String>,
    val rootIndicator: RootIndicator,
    val uptimeMs: Long?,
    val buildDateMs: Long?,
    val releaseYear: Int?,
    val deepSleepPercent: Int?,
    val selinuxStatus: String?,
    val bootCount: Int?,
    val interactive: Boolean?,
    val idleMode: Boolean?
)

enum class RootHeuristic { LIKELY_PRESENT, NOT_DETECTED, UNKNOWN }
data class RootIndicator(val heuristic: RootHeuristic)

// ---------- CPU ----------
data class CpuInfo(
    val name: String?,
    val architecture: String?,
    val abi: String?,
    val coreCount: Int,
    val supportedAbis: List<String>,
    val currentFreqMhz: Double?,
    val minFreqMhz: Double?,
    val maxFreqMhz: Double?,
    val perCore: List<CoreInfo>,
    val usagePercent: Double?
)

data class CoreInfo(
    val index: Int,
    val currentMhz: Double?,
    val maxMhz: Double?
)

// ---------- GPU ----------
data class GpuInfo(
    val renderer: String?,
    val vendor: String?,
    val glesVersion: String?,
    val vulkan: String?,
    val extensions: List<String>
)

// ---------- RAM ----------
data class RamInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val thresholdBytes: Long?,
    val lowMemory: Boolean?
)

// ---------- Storage ----------
data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usagePercent: Int
)

// ---------- Battery ----------
data class BatteryInfo(
    val percent: Int?,
    val statusKey: String?,
    val charging: Boolean?,
    val sourceKey: String?,
    val temperatureCelsius: Float?,
    val voltageMv: Int?,
    val currentMa: Int?,
    val technology: String?,
    val healthKey: String?,
    val capacityPercent: Int?,
    val batterySaverOn: Boolean?,
    val remainingMah: Int?
)

// ---------- Thermal ----------
enum class ThermalSeverity { NORMAL, WARM, HOT, CRITICAL }

data class ThermalZone(
    val name: String,
    val type: String?,
    val temperatureCelsius: Float
)

data class ThermalSnapshot(
    val batteryCelsius: Float?,
    val ambientCelsius: Float?,
    val zones: List<ThermalZone>,
    val severity: ThermalSeverity,
    val thresholds: ThermalThresholds
)

data class ThermalThresholds(
    val warmC: Float = 38f,
    val hotC: Float = 45f,
    val criticalC: Float = 52f
)

// ---------- Display ----------
data class DisplayInfo(
    val widthPx: Int?,
    val heightPx: Int?,
    val density: Float?,
    val densityDpi: Int?,
    val refreshRateHz: Float?,
    val modes: List<String>,
    val hdrTypes: List<String>,
    val physicalSizeInches: Double?,
    val orientationKey: String,
    val densityBucket: String?,
    val smallestWidthDp: Int?
)

// ---------- Camera ----------
data class CameraSummary(
    val totalCount: Int,
    val cameras: List<CameraDetail>
)

data class CameraDetail(
    val id: String,
    val facingKey: String,
    val megapixelsCalculated: Double?,
    val sensorSizeMm: String?,
    val focalLengthsMm: List<Float>,
    val apertures: List<Float>,
    val autofocusSupported: Boolean?,
    val flashSupported: Boolean?,
    val outputSizes: List<String>
)

// ---------- Sensors ----------
data class SensorSpec(
    val type: Int,
    val name: String,
    val vendor: String?,
    val version: Int,
    val powerMa: Float,
    val resolution: Float,
    val maxRange: Float,
    val wakeUp: Boolean
)

// ---------- Network ----------
data class NetworkInfo(
    val connected: Boolean?,
    val transportKey: String?,
    val linkDownstreamKbps: Int?,
    val metered: Boolean?,
    val vpnActive: Boolean?,
    val wifiLinkSpeedMbps: Int?,
    val bluetoothSupported: Boolean,
    val bluetoothEnabled: Boolean?,
    val nfcSupported: Boolean,
    val nfcEnabled: Boolean?,
    val gpsSupported: Boolean,
    val usbSupported: Boolean,
    val usbDeviceCount: Int?
)

// ---------- Aggregates ----------
data class DashboardData(
    val device: DeviceInfo,
    val cpu: CpuInfo,
    val ram: RamInfo,
    val storage: StorageInfo,
    val battery: BatteryInfo,
    val thermal: ThermalSnapshot
)

data class LiveMetrics(
    val cpuUsagePercent: Double?,
    val cpuName: String?,
    val ram: RamInfo,
    val storage: StorageInfo,
    val battery: BatteryInfo,
    val thermal: ThermalSnapshot
)

// ---------- Refurbishment check ----------
enum class RefurbStatus { GENUINE_LIKELY, SUSPICIOUS, UNKNOWN }

data class RefurbItem(
    val componentKey: String,
    val status: RefurbStatus,
    val detail: String?
)

data class RefurbReport(
    val items: List<RefurbItem>,
    val overall: RefurbStatus,
    val score: Int,
    val suspiciousCount: Int,
    val unknownCount: Int
)

data class ExternalStorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val removable: Boolean
)

data class StorageDetails(
    val internalStorage: StorageInfo,
    val external: ExternalStorageInfo?
)
