package com.droidspec.domain.usecase

import com.droidspec.core.util.pollLatest
import com.droidspec.data.battery.BatteryDataSource
import com.droidspec.domain.model.BatteryInfo
import com.droidspec.presentation.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveBatteryUseCase(
    private val dataSource: BatteryDataSource,
    private val settings: SettingsRepository
) {
    operator fun invoke(): Flow<BatteryInfo> =
        settings.settings.map { it.refreshMs }.pollLatest { dataSource.snapshot() }
}
