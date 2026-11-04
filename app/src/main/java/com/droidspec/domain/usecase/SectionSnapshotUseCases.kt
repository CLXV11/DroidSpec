package com.droidspec.domain.usecase

import com.droidspec.data.battery.BatteryDataSource
import com.droidspec.data.camera.CameraDataSource
import com.droidspec.data.cpu.CpuDataSource
import com.droidspec.data.device.DeviceDataSource
import com.droidspec.data.display.DisplayDataSource
import com.droidspec.data.gpu.GpuDataSource
import com.droidspec.data.memory.MemoryDataSource
import com.droidspec.data.network.NetworkDataSource
import com.droidspec.data.refurb.RefurbDataSource
import com.droidspec.data.sensors.SensorsDataSource
import com.droidspec.data.storage.StorageDataSource
import com.droidspec.data.thermal.ThermalDataSource
import com.droidspec.domain.model.BatteryInfo
import com.droidspec.domain.model.CameraSummary
import com.droidspec.domain.model.CpuInfo
import com.droidspec.domain.model.DeviceInfo
import com.droidspec.domain.model.DisplayInfo
import com.droidspec.domain.model.GpuInfo
import com.droidspec.domain.model.NetworkInfo
import com.droidspec.domain.model.RefurbReport
import android.os.Build
import com.droidspec.domain.policy.ExpectedComponents
import com.droidspec.domain.policy.RefurbAnalyzer
import com.droidspec.domain.model.RamInfo
import com.droidspec.domain.model.SensorSpec
import com.droidspec.domain.model.StorageDetails
import com.droidspec.domain.model.StorageInfo
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.model.ThermalThresholds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Simple snapshot use cases for mostly-static sections. */
class SectionSnapshotUseCases(
    val device: DeviceDataSource,
    val cpu: CpuDataSource,
    val gpu: GpuDataSource,
    val memory: MemoryDataSource,
    val storage: StorageDataSource,
    val battery: BatteryDataSource,
    val thermal: ThermalDataSource,
    val display: DisplayDataSource,
    val camera: CameraDataSource,
    val sensors: SensorsDataSource,
    val network: NetworkDataSource,
    val refurb: RefurbDataSource
) {
    suspend fun device(): DeviceInfo = withContext(Dispatchers.IO) { device.snapshot() }

    suspend fun cpu(): CpuInfo = withContext(Dispatchers.IO) {
        cpu.snapshotStatic().copy(usagePercent = cpu.measureUsage())
    }

    suspend fun gpu(): GpuInfo = withContext(Dispatchers.IO) { gpu.snapshot() }
    suspend fun ram(): RamInfo = withContext(Dispatchers.IO) { memory.snapshot() }
    suspend fun storage(): StorageInfo = withContext(Dispatchers.IO) { storage.snapshot() }

    suspend fun storageDetails(): StorageDetails = withContext(Dispatchers.IO) {
        StorageDetails(storage.snapshot(), storage.external())
    }
    suspend fun battery(): BatteryInfo = withContext(Dispatchers.IO) { battery.snapshot() }

    suspend fun thermal(): ThermalSnapshot = withContext(Dispatchers.IO) {
        ThermalDataSource.buildSnapshot(
            battery = thermal.batteryTemperature(),
            ambient = null,
            zones = thermal.thermalZones(),
            thresholds = ThermalThresholds()
        )
    }

    suspend fun display(): DisplayInfo = withContext(Dispatchers.IO) { display.snapshot() }
    suspend fun camera(): CameraSummary = withContext(Dispatchers.IO) { camera.snapshot() }
    suspend fun sensorList(): List<SensorSpec> = withContext(Dispatchers.IO) { sensors.snapshot() }
    suspend fun network(): NetworkInfo = withContext(Dispatchers.IO) { network.snapshot() }

    suspend fun refurb(): RefurbReport = withContext(Dispatchers.IO) {
        RefurbAnalyzer.analyze(
            refurb.collect(),
            ExpectedComponents.forDevice(Build.DEVICE)
        )
    }
}
