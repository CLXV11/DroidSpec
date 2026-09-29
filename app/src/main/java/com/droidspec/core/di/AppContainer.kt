package com.droidspec.core.di

import android.content.Context
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
import com.droidspec.domain.benchmark.BenchmarkEngine
import com.droidspec.domain.report.ReportDataCollector
import com.droidspec.domain.search.SearchEngine
import com.droidspec.domain.usecase.ObserveBatteryUseCase
import com.droidspec.domain.usecase.ObserveDashboardUseCase
import com.droidspec.domain.usecase.ObserveLiveMetricsUseCase
import com.droidspec.domain.usecase.ObserveThermalUseCase
import com.droidspec.domain.usecase.SectionSnapshotUseCases
import com.droidspec.presentation.settings.SettingsRepository

/**
 * Manual dependency container. Keeps the app DI-library-free while still
 * providing a single, testable composition root.
 */
class AppContainer(context: Context) {
    private val app = context.applicationContext

    fun appContext(): Context = app

    val settingsRepository = SettingsRepository(app)

    val deviceDataSource = DeviceDataSource(app)
    val cpuDataSource = CpuDataSource()
    val gpuDataSource = GpuDataSource(app)
    val memoryDataSource = MemoryDataSource(app)
    val storageDataSource = StorageDataSource(app)
    val batteryDataSource = BatteryDataSource(app)
    val thermalDataSource = ThermalDataSource(app)
    val displayDataSource = DisplayDataSource(app)
    val cameraDataSource = CameraDataSource(app)
    val sensorsDataSource = SensorsDataSource(app)
    val networkDataSource = NetworkDataSource(app)
    val refurbDataSource = RefurbDataSource(app, batteryDataSource)

    val observeBattery = ObserveBatteryUseCase(batteryDataSource, settingsRepository)
    val observeThermal = ObserveThermalUseCase(thermalDataSource, settingsRepository)
    val observeDashboard = ObserveDashboardUseCase(
        deviceDataSource, cpuDataSource, memoryDataSource, storageDataSource,
        batteryDataSource, thermalDataSource, settingsRepository
    )
    val observeLiveMetrics = ObserveLiveMetricsUseCase(
        cpuDataSource, memoryDataSource, storageDataSource, batteryDataSource,
        thermalDataSource, settingsRepository
    )

    val snapshots = SectionSnapshotUseCases(
        deviceDataSource, cpuDataSource, gpuDataSource, memoryDataSource,
        storageDataSource, batteryDataSource, thermalDataSource, displayDataSource,
        cameraDataSource, sensorsDataSource, networkDataSource, refurbDataSource
    )

    val reportCollector = ReportDataCollector(snapshots)
    val searchEngine = SearchEngine(snapshots)
    val benchmarkEngine = BenchmarkEngine(app)
}
