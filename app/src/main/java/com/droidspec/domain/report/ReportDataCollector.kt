package com.droidspec.domain.report

import com.droidspec.domain.model.BatteryInfo
import com.droidspec.domain.model.CameraSummary
import com.droidspec.domain.model.CpuInfo
import com.droidspec.domain.model.DeviceInfo
import com.droidspec.domain.model.DisplayInfo
import com.droidspec.domain.model.GpuInfo
import com.droidspec.domain.model.NetworkInfo
import com.droidspec.domain.model.RamInfo
import com.droidspec.domain.model.SensorSpec
import com.droidspec.domain.model.StorageInfo
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.usecase.SectionSnapshotUseCases
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DeviceReport(
    val device: DeviceInfo,
    val cpu: CpuInfo,
    val gpu: GpuInfo,
    val ram: RamInfo,
    val storage: StorageInfo,
    val battery: BatteryInfo,
    val thermal: ThermalSnapshot,
    val display: DisplayInfo,
    val sensors: List<SensorSpec>,
    val network: NetworkInfo,
    val generatedBy: String,
    val generatedAtEpochMs: Long
)

class ReportDataCollector(private val snapshots: SectionSnapshotUseCases) {
    suspend fun collect(): DeviceReport = coroutineScope {
        val device = async { snapshots.device() }
        val cpu = async { snapshots.cpu() }
        val gpu = async { snapshots.gpu() }
        val ram = async { snapshots.ram() }
        val storage = async { snapshots.storage() }
        val battery = async { snapshots.battery() }
        val thermal = async { snapshots.thermal() }
        val display = async { snapshots.display() }
        val sensors = async { snapshots.sensorList() }
        val network = async { snapshots.network() }
        DeviceReport(
            device = device.await(),
            cpu = cpu.await(),
            gpu = gpu.await(),
            ram = ram.await(),
            storage = storage.await(),
            battery = battery.await(),
            thermal = thermal.await(),
            display = display.await(),
            sensors = sensors.await(),
            network = network.await(),
            generatedBy = "DroidSpec Internal Report",
            generatedAtEpochMs = System.currentTimeMillis()
        )
    }
}
