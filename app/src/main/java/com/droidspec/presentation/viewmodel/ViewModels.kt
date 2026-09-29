package com.droidspec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidspec.core.model.ScreenState
import com.droidspec.data.network.NetworkDataSource
import com.droidspec.data.sensors.SensorsDataSource
import com.droidspec.domain.benchmark.BenchmarkEngine
import com.droidspec.domain.model.BatteryInfo
import com.droidspec.domain.model.CameraSummary
import com.droidspec.domain.model.CpuInfo
import com.droidspec.domain.model.DashboardData
import com.droidspec.domain.model.DeviceInfo
import com.droidspec.domain.model.DisplayInfo
import com.droidspec.domain.model.GpuInfo
import com.droidspec.domain.model.LiveMetrics
import com.droidspec.domain.model.NetworkInfo
import com.droidspec.domain.model.RamInfo
import com.droidspec.domain.model.RefurbReport
import com.droidspec.domain.model.SensorSpec
import com.droidspec.domain.model.StorageDetails
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.report.DeviceReport
import com.droidspec.domain.report.ReportDataCollector
import com.droidspec.domain.search.SearchEngine
import com.droidspec.domain.usecase.ObserveBatteryUseCase
import com.droidspec.domain.usecase.ObserveDashboardUseCase
import com.droidspec.domain.usecase.ObserveLiveMetricsUseCase
import com.droidspec.domain.usecase.ObserveThermalUseCase
import com.droidspec.domain.usecase.SectionSnapshotUseCases
import com.droidspec.presentation.report.ReportWriter
import com.droidspec.presentation.settings.AppSettings
import com.droidspec.presentation.settings.ExportFormat
import com.droidspec.presentation.settings.LanguageMode
import com.droidspec.presentation.settings.SettingsRepository
import com.droidspec.presentation.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun <T> kotlinx.coroutines.flow.Flow<T>.toScreenState() =
    map<T, ScreenState<T>> { ScreenState(loading = false, data = it) }
        .catch { emit(ScreenState(loading = false, error = it.message)) }

class DashboardViewModel(
    observeDashboard: ObserveDashboardUseCase
) : ViewModel() {
    val state: StateFlow<ScreenState<DashboardData>> = observeDashboard()
        .toScreenState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState(loading = true))
}

class LiveMonitorViewModel(
    observeLiveMetrics: ObserveLiveMetricsUseCase
) : ViewModel() {
    val state: StateFlow<ScreenState<LiveMetrics>> = observeLiveMetrics()
        .toScreenState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState(loading = true))
}

class BatteryViewModel(observeBattery: ObserveBatteryUseCase) : ViewModel() {
    val state: StateFlow<ScreenState<BatteryInfo>> = observeBattery()
        .toScreenState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState(loading = true))
}

class ThermalViewModel(observeThermal: ObserveThermalUseCase) : ViewModel() {
    val state: StateFlow<ScreenState<ThermalSnapshot>> = observeThermal()
        .toScreenState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState(loading = true))
}

class NetworkViewModel(dataSource: NetworkDataSource) : ViewModel() {
    val state: StateFlow<ScreenState<NetworkInfo>> = dataSource.observe()
        .toScreenState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState(loading = true))
}

abstract class SnapshotViewModel<T>(private val load: suspend () -> T) : ViewModel() {
    protected val _state = MutableStateFlow(ScreenState<T>(loading = true))
    val state = _state.asStateFlow()

    fun refreshNow() {
        viewModelScope.launch {
            _state.value = ScreenState(loading = true)
            runCatching { load() }
                .onSuccess { _state.value = ScreenState(loading = false, data = it) }
                .onFailure { _state.value = ScreenState(loading = false, error = it.message) }
        }
    }

    init {
        refreshNow()
    }
}

class CpuViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<CpuInfo>({ s.cpu() })
class GpuViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<GpuInfo>({ s.gpu() })
class MemoryViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<RamInfo>({ s.ram() })
class StorageViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<StorageDetails>({ s.storageDetails() })
class DisplayViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<DisplayInfo>({ s.display() })
class CameraViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<CameraSummary>({ s.camera() })
class DeviceViewModel(s: SectionSnapshotUseCases) : SnapshotViewModel<DeviceInfo>({ s.device() })
class SensorsViewModel(s: SectionSnapshotUseCases) :
    SnapshotViewModel<List<SensorSpec>>({ s.sensorList() })

class RefurbViewModel(s: SectionSnapshotUseCases) :
    SnapshotViewModel<RefurbReport>({ s.refurb() })

class SensorDetailViewModel(
    private val dataSource: SensorsDataSource,
    private val sensorType: Int
) : ViewModel() {
    private val _values = MutableStateFlow<FloatArray?>(null)
    val values = _values.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.observeSensorValues(sensorType).collect { _values.value = it }
        }
    }
}

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repository.setTheme(mode) }
    fun setLanguage(mode: LanguageMode) = viewModelScope.launch { repository.setLanguage(mode) }
    fun setRefreshMs(ms: Long) = viewModelScope.launch { repository.setRefreshMs(ms) }
    fun setExportFormat(format: ExportFormat) =
        viewModelScope.launch { repository.setExportFormat(format) }
}

class SearchViewModel(private val engine: SearchEngine) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _results = MutableStateFlow<List<com.droidspec.domain.search.SearchEntry>>(emptyList())
    val results = _results.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching = _searching.asStateFlow()

    private var index: List<com.droidspec.domain.search.SearchEntry>? = null

    fun onQueryChange(q: String) {
        _query.value = q
        if (q.trim().length < 2) {
            _results.value = emptyList()
            return
        }
        viewModelScope.launch {
            if (index == null) {
                _searching.value = true
                index = runCatching { engine.buildIndex() }.getOrDefault(emptyList())
                _searching.value = false
            }
            _results.value = engine.filter(index.orEmpty(), q)
        }
    }
}

class BenchmarkViewModel(private val engine: BenchmarkEngine) : ViewModel() {
    private val _state =
        MutableStateFlow<BenchmarkEngine.BenchState>(BenchmarkEngine.BenchState.Idle)
    val state = _state.asStateFlow()

    fun start() = engine.run(viewModelScope) { _state.value = it }
    fun cancel() = engine.cancel()

    override fun onCleared() {
        engine.cancel()
        super.onCleared()
    }
}

class ReportsViewModel(
    private val collector: ReportDataCollector,
    private val settings: SettingsRepository,
    private val reportWriter: ReportWriter
) : ViewModel() {
    private val _report = MutableStateFlow<DeviceReport?>(null)
    val report = _report.asStateFlow()

    private val _text = MutableStateFlow<String?>(null)
    val text = _text.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage = _savedMessage.asStateFlow()

    val exportFormat: StateFlow<ExportFormat> = settings.settings
        .map { it.exportFormat }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExportFormat.TXT)

    fun setExportFormat(format: ExportFormat) =
        viewModelScope.launch { settings.setExportFormat(format) }

    fun generate() {
        viewModelScope.launch {
            _busy.value = true
            runCatching {
                val r = collector.collect()
                _report.value = r
                val format = settings.settings.first().exportFormat
                _text.value = when (format) {
                    ExportFormat.TXT -> reportWriter.toText(r)
                    ExportFormat.JSON -> reportWriter.toJson(r)
                }
            }
            _busy.value = false
        }
    }

    fun save() {
        val text = _text.value ?: return
        viewModelScope.launch {
            _savedMessage.value = runCatching { reportWriter.saveToFile(text) }
                .getOrNull()
        }
    }

    fun consumeSavedMessage() {
        _savedMessage.value = null
    }
}
