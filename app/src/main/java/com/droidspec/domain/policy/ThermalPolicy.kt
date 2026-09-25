package com.droidspec.domain.policy

import com.droidspec.domain.model.ThermalSeverity
import com.droidspec.domain.model.ThermalThresholds

/** Application-level severity classification. Pure and unit-testable. */
object ThermalPolicy {
    fun classify(temp: Float?, thresholds: ThermalThresholds): ThermalSeverity = when {
        temp == null -> ThermalSeverity.NORMAL
        temp >= thresholds.criticalC -> ThermalSeverity.CRITICAL
        temp >= thresholds.hotC -> ThermalSeverity.HOT
        temp >= thresholds.warmC -> ThermalSeverity.WARM
        else -> ThermalSeverity.NORMAL
    }
}
