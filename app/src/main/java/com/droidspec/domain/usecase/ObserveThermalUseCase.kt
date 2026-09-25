package com.droidspec.domain.usecase

import com.droidspec.core.util.pollLatest
import com.droidspec.data.thermal.ThermalDataSource
import com.droidspec.domain.model.ThermalSnapshot
import com.droidspec.domain.model.ThermalThresholds
import com.droidspec.presentation.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class ObserveThermalUseCase(
    private val dataSource: ThermalDataSource,
    private val settings: SettingsRepository
) {
    operator fun invoke(thresholds: ThermalThresholds = ThermalThresholds()): Flow<ThermalSnapshot> =
        settings.settings.map { it.refreshMs }.pollLatest {
            val ambient = dataSource.observeAmbientTemperature().first()
            ThermalDataSource.buildSnapshot(
                battery = dataSource.batteryTemperature(),
                ambient = ambient,
                zones = dataSource.thermalZones(),
                thresholds = thresholds
            )
        }
}
