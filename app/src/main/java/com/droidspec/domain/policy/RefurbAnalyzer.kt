package com.droidspec.domain.policy

import com.droidspec.domain.model.RefurbItem
import com.droidspec.domain.model.RefurbReport
import com.droidspec.domain.model.RefurbStatus

/** Raw hardware signals gathered via public APIs and best-effort sysfs reads. */
data class RefurbSignals(
    val touchControllers: List<String>,
    val panelInfo: String?,
    val batteryCycles: Int?,
    val batteryHealthKey: String?,
    val capacityPercent: Int?,
    val physicalSizeInches: Double?,
    val cameraCount: Int?,
    val usbState: String?,
    val fingerprintSupported: Boolean?,
    val microphone: Boolean?,
    val speaker: Boolean?,
    val vibrator: Boolean?,
    val chargerType: String?,
    val nfcPresent: Boolean?
)

/**
 * Professional refurbishment analyzer v2:
 *  - expected-vs-actual checks against the labeled local device database
 *  - trust score (0-100): 100, −25 per SUSPICIOUS, −10 per UNKNOWN
 * Every verdict stays advisory; details are raw tokens (e.g. "exp=4 got=2").
 */
object RefurbAnalyzer {

    fun analyze(signals: RefurbSignals, expected: ExpectedSpec? = null): RefurbReport {
        val items = mutableListOf<RefurbItem>()

        // --- display chain ---
        items += if (signals.touchControllers.isEmpty()) {
            RefurbItem("touch", RefurbStatus.UNKNOWN, null)
        } else {
            RefurbItem("touch", RefurbStatus.GENUINE_LIKELY, signals.touchControllers.joinToString(" · "))
        }
        items += if (signals.panelInfo.isNullOrBlank()) {
            RefurbItem("display", RefurbStatus.UNKNOWN, null)
        } else {
            RefurbItem("display", RefurbStatus.GENUINE_LIKELY, signals.panelInfo)
        }
        val size = signals.physicalSizeInches
        items += when {
            size == null -> RefurbItem("display_size", RefurbStatus.UNKNOWN, null)
            size in 3.0..13.0 -> RefurbItem("display_size", RefurbStatus.GENUINE_LIKELY, "%.1f".format(size))
            else -> RefurbItem("display_size", RefurbStatus.SUSPICIOUS, "%.1f".format(size))
        }

        // --- battery ---
        val health = signals.batteryHealthKey
        when {
            health == "dead" || health == "cold" || health == "overheat" ->
                items += RefurbItem("battery", RefurbStatus.SUSPICIOUS, health)
            signals.capacityPercent != null && signals.capacityPercent < 80 ->
                items += RefurbItem("battery", RefurbStatus.SUSPICIOUS, "cap ${signals.capacityPercent}%")
            signals.batteryCycles != null && signals.batteryCycles > 800 ->
                items += RefurbItem("battery", RefurbStatus.SUSPICIOUS, "cycles ${signals.batteryCycles}")
            else -> items += RefurbItem(
                "battery", RefurbStatus.GENUINE_LIKELY,
                listOfNotNull(
                    health?.let { "health=$it" },
                    signals.capacityPercent?.let { "cap=$it%" },
                    signals.batteryCycles?.let { "cycles=$it" }
                ).joinToString(" ").ifBlank { null }
            )
        }

        // --- power / ports ---
        items += if (signals.usbState.isNullOrBlank()) {
            RefurbItem("usb", RefurbStatus.UNKNOWN, null)
        } else {
            RefurbItem("usb", RefurbStatus.GENUINE_LIKELY, signals.usbState)
        }
        items += if (signals.chargerType.isNullOrBlank()) {
            RefurbItem("charger", RefurbStatus.UNKNOWN, null)
        } else {
            RefurbItem("charger", RefurbStatus.GENUINE_LIKELY, signals.chargerType)
        }

        // --- cameras: expected vs actual (labeled DB) ---
        val cams = signals.cameraCount
        val expCams = expected?.cameras
        items += when {
            cams == null -> RefurbItem("camera", RefurbStatus.UNKNOWN, null)
            expCams != null && cams != expCams ->
                RefurbItem("camera", RefurbStatus.SUSPICIOUS, "exp=$expCams got=$cams")
            cams == 0 -> RefurbItem("camera", RefurbStatus.SUSPICIOUS, "0")
            else -> RefurbItem("camera", RefurbStatus.GENUINE_LIKELY, cams.toString())
        }

        // --- fingerprint: a replaced screen frequently kills it ---
        val fp = signals.fingerprintSupported
        val expFp = expected?.fingerprint
        items += when {
            expFp == true && fp == false ->
                RefurbItem("fingerprint", RefurbStatus.SUSPICIOUS, "expected, missing")
            fp == true -> RefurbItem("fingerprint", RefurbStatus.GENUINE_LIKELY, "present")
            fp == false && expFp == null -> RefurbItem("fingerprint", RefurbStatus.UNKNOWN, "absent")
            else -> RefurbItem("fingerprint", RefurbStatus.UNKNOWN, null)
        }

        // --- NFC expectation ---
        if (expected?.nfc == true) {
            items += when (signals.nfcPresent) {
                true -> RefurbItem("nfc", RefurbStatus.GENUINE_LIKELY, "present")
                false -> RefurbItem("nfc", RefurbStatus.SUSPICIOUS, "expected, missing")
                null -> RefurbItem("nfc", RefurbStatus.UNKNOWN, null)
            }
        }

        // --- presence sanity: mic / speaker / vibrator ---
        items += presenceItem("microphone", signals.microphone)
        items += presenceItem("speaker", signals.speaker)
        items += presenceItem("vibrator", signals.vibrator)

        val suspicious = items.count { it.status == RefurbStatus.SUSPICIOUS }
        val unknown = items.count { it.status == RefurbStatus.UNKNOWN }
        val overall = when {
            suspicious > 0 -> RefurbStatus.SUSPICIOUS
            unknown >= 4 -> RefurbStatus.UNKNOWN
            else -> RefurbStatus.GENUINE_LIKELY
        }
        return RefurbReport(
            items = items,
            overall = overall,
            score = score(items),
            suspiciousCount = suspicious,
            unknownCount = unknown
        )
    }

    /** Trust score: 100, −25 per SUSPICIOUS, −10 per UNKNOWN. Pure. */
    fun score(items: List<RefurbItem>): Int {
        var s = 100
        items.forEach {
            when (it.status) {
                RefurbStatus.SUSPICIOUS -> s -= 25
                RefurbStatus.UNKNOWN -> s -= 10
                RefurbStatus.GENUINE_LIKELY -> Unit
            }
        }
        return s.coerceIn(0, 100)
    }

    private fun presenceItem(key: String, present: Boolean?): RefurbItem = when (present) {
        true -> RefurbItem(key, RefurbStatus.GENUINE_LIKELY, "present")
        false -> RefurbItem(key, RefurbStatus.UNKNOWN, "absent")
        null -> RefurbItem(key, RefurbStatus.UNKNOWN, null)
    }
}
