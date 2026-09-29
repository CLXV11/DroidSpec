package com.droidspec.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.droidspec.presentation.report.ReportWriter
import com.droidspec.presentation.viewmodel.BatteryViewModel
import com.droidspec.presentation.viewmodel.BenchmarkViewModel
import com.droidspec.presentation.viewmodel.CameraViewModel
import com.droidspec.presentation.viewmodel.CpuViewModel
import com.droidspec.presentation.viewmodel.DashboardViewModel
import com.droidspec.presentation.viewmodel.DeviceViewModel
import com.droidspec.presentation.viewmodel.DisplayViewModel
import com.droidspec.presentation.viewmodel.GpuViewModel
import com.droidspec.presentation.viewmodel.LiveMonitorViewModel
import com.droidspec.presentation.viewmodel.MemoryViewModel
import com.droidspec.presentation.viewmodel.NetworkViewModel
import com.droidspec.presentation.viewmodel.RefurbViewModel
import com.droidspec.presentation.viewmodel.ReportsViewModel
import com.droidspec.presentation.viewmodel.SearchViewModel
import com.droidspec.presentation.viewmodel.SensorsViewModel
import com.droidspec.presentation.viewmodel.SettingsViewModel
import com.droidspec.presentation.viewmodel.StorageViewModel
import com.droidspec.presentation.viewmodel.ThermalViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val c = container
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(c.observeDashboard) as T
            modelClass.isAssignableFrom(LiveMonitorViewModel::class.java) ->
                LiveMonitorViewModel(c.observeLiveMetrics) as T
            modelClass.isAssignableFrom(BatteryViewModel::class.java) ->
                BatteryViewModel(c.observeBattery) as T
            modelClass.isAssignableFrom(ThermalViewModel::class.java) ->
                ThermalViewModel(c.observeThermal) as T
            modelClass.isAssignableFrom(CpuViewModel::class.java) ->
                CpuViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(GpuViewModel::class.java) ->
                GpuViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(MemoryViewModel::class.java) ->
                MemoryViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(StorageViewModel::class.java) ->
                StorageViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(DisplayViewModel::class.java) ->
                DisplayViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(CameraViewModel::class.java) ->
                CameraViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(DeviceViewModel::class.java) ->
                DeviceViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(RefurbViewModel::class.java) ->
                RefurbViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(SensorsViewModel::class.java) ->
                SensorsViewModel(c.snapshots) as T
            modelClass.isAssignableFrom(NetworkViewModel::class.java) ->
                NetworkViewModel(c.networkDataSource) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(c.settingsRepository) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(c.searchEngine) as T
            modelClass.isAssignableFrom(BenchmarkViewModel::class.java) ->
                BenchmarkViewModel(c.benchmarkEngine) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) ->
                ReportsViewModel(
                    c.reportCollector,
                    c.settingsRepository,
                    ReportWriter(c.appContext())
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${'$'}{modelClass.name}")
        }
    }
}
