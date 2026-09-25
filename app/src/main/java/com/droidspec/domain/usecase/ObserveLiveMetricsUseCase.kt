package com.droidspec.domain.usecase

import com.droidspec.core.util.pollLatest
import com.droidspec.data.battery.BatteryDataSource
import com.droidspec.data.cpu.CpuDataSource
import com.droidspec.data.memory.MemoryDataSource
import com.droidspec.data.storage.StorageDataSource
import com.droidspec.data.thermal.ThermalDataSource
import com.droidspec.domain.model.LiveMetrics
import com.droidspec.domain.model.ThermalThresholds
import com.droidspec.presentation.settings.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveLiveMetricsUseCase(
    private val cpu: CpuDataSource,
    private val memory: MemoryDataSource,
    private val storage: StorageDataSource,
    private val battery: BatteryDataSource,
    private val thermal: ThermalDataSource,
    private val settings: SettingsRepository
) {
    operator fun invoke(): Flow<LiveMetrics> =
        settings.settings.map { it.refreshMs }.pollLatest {
            coroutineScope {
                val usage = async { cpu.measureUsage() }
                val cpuStatic = async { cpu.snapshotStatic() }
                val ram = async { memory.snapshot() }
                val st = async { storage.snapshot() }
                val batt = async { battery.snapshot() }
                val th = async {
                    ThermalDataSource.buildSnapshot(
                        battery = thermal.batteryTemperature(),
                        ambient = null,
                        zones = thermal.thermalZones(),
                        thresholds = ThermalThresholds()
                    )
                }
                LiveMetrics(
                    cpuUsagePercent = usage.await(),
                    cpuName = cpuStatic.await().name,
                    ram = ram.await(),
                    storage = st.await(),
                    battery = batt.await(),
                    thermal = th.await()
                )
            }
        }
}
