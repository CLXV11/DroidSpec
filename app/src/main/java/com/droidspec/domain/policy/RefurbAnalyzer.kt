package com.droidspec.domain.policy

import com.droidspec.domain.model.RefurbItem
import com.droidspec.domain.model.RefurbReport
import com.droidspec.domain.model.RefurbStatus

/** Raw hardware signals from public APIs + best-effort deep reads. */
data class RefurbSignals(
    val touchControllers: List<String>,
    val panelVendor: String?,
    val panelRaw: String?,
    val batteryCycles: Int?,
    val batteryHealthKey: String?,
    val remainingMah: Int?,
    val chargePercent: Int?,
    val batteryVoltageMv: Int?,
    val batteryTempC: Float?,
    val cameraCount: Int?,
    val cameraOisSupported: Boolean?,
    val usbState: String?,
    val chargerType: String?,
    val fingerprintSupported: Boolean?,
    val microphone: Boolean?,
    val speaker: Boolean?,
    val vibratorCaps: String?,
    val nfcPresent: Boolean?,
    val physicalSizeInches: Double?
)

/**
 * Deep refurbishment audit. No vague labels: every item carries the RAW
 * technical readout plus a verdict derived from the labeled reference DB
 * (or NO_REFERENCE when no trustworthy reference exists).
 */
object RefurbAnalyzer {

    fun analyze(signals: RefurbSignals, expected: ExpectedSpec? = null): RefurbReport {
        val items = mutableListOf<RefurbItem>()

        // ---- display chain ----
        val panelReadout = listOfNotNull(
            signals.panelVendor?.let { "vendor=$it" },
            signals.panelRaw?.takeIf { it.isNotBlank() }?.let { "raw=${it.take(60)}" }
        ).joinToString("  ").ifBlank { null }
        val expPanels = expected?.panelVendors
        items += when {
            signals.panelRaw.isNullOrBlank() && signals.panelVendor == null ->
                RefurbItem("panel", RefurbStatus.UNREADABLE, null, null)
            expPanels != null && signals.panelVendor != null && signals.panelVendor !in expPanels ->
                RefurbItem("panel", RefurbStatus.MISMATCHED, panelReadout, "panel_vendor")
            else -> RefurbItem("panel", RefurbStatus.NO_REFERENCE, panelReadout, null)
        }

        val touchReadout = signals.touchControllers.takeIf { it.isNotEmpty() }
            ?.joinToString(" · ") { it }
        val expTouch = expected?.touchControllers
        items += when {
            signals.touchControllers.isEmpty() ->
                RefurbItem("touch", RefurbStatus.UNREADABLE, null, null)
            expTouch != null && signals.touchControllers.none { c ->
                expTouch.any { e -> c.lowercase().contains(e.lowercase()) }
            } -> RefurbItem("touch", RefurbStatus.MISMATCHED, touchReadout, "touch_driver")
            else -> RefurbItem("touch", RefurbStatus.NO_REFERENCE, touchReadout, null)
        }

        val size = signals.physicalSizeInches
        items += when {
            size == null -> RefurbItem("display_size", RefurbStatus.UNREADABLE, null, null)
            size in 3.0..13.0 -> RefurbItem(
                "display_size", RefurbStatus.NO_REFERENCE, "%.1f in".format(size), null
            )
            else -> RefurbItem("display_size", RefurbStatus.MISMATCHED, "%.1f in".format(size), null)
        }

        // ---- battery: real wear math (cycle count, health, full-vs-design) ----
        val fullMah = estimateFullMah(signals.remainingMah, signals.chargePercent)
        val designMah = expected?.batteryDesignMah
        val battReadout = listOfNotNull(
            signals.batteryCycles?.let { "cycles=$it" },
            designMah?.let { "design=${it}mAh" },
            fullMah?.let { "full≈${it}mAh" },
            signals.batteryHealthKey?.let { "health=$it" },
            signals.batteryVoltageMv?.let { "v=${it}mV" },
            signals.batteryTempC?.let { "t=${"%.1f".format(it)}C" }
        ).joinToString("  ").ifBlank { null }
        val worn = signals.batteryHealthKey in listOf("dead", "cold", "overheat") ||
            (signals.batteryCycles != null && signals.batteryCycles > 800) ||
            (designMah != null && fullMah != null && fullMah < designMah * 0.8)
        items += when {
            battReadout == null -> RefurbItem("battery", RefurbStatus.UNREADABLE, null, null)
            worn -> RefurbItem("battery", RefurbStatus.MISMATCHED, battReadout, "battery_wear")
            else -> RefurbItem("battery", RefurbStatus.NO_REFERENCE, battReadout, null)
        }

        // ---- power ----
        items += if (signals.usbState.isNullOrBlank()) {
            RefurbItem("usb", RefurbStatus.UNREADABLE, null, null)
        } else {
            RefurbItem("usb", RefurbStatus.NO_REFERENCE, signals.usbState, null)
        }
        items += if (signals.chargerType.isNullOrBlank()) {
            RefurbItem("charger", RefurbStatus.UNREADABLE, null, null)
        } else {
            RefurbItem("charger", RefurbStatus.NO_REFERENCE, signals.chargerType, null)
        }

        // ---- cameras ----
        val camReadout = buildList {
            signals.cameraCount?.let { add("count=$it") }
            signals.cameraOisSupported?.let { add(if (it) "OIS=yes" else "OIS=no") }
        }.joinToString("  ").ifBlank { null }
        val expCams = expected?.cameras
        items += when {
            signals.cameraCount == null -> RefurbItem("camera", RefurbStatus.UNREADABLE, null, null)
            expCams != null && signals.cameraCount != expCams ->
                RefurbItem("camera", RefurbStatus.MISMATCHED, camReadout, "camera_count")
            else -> RefurbItem("camera", RefurbStatus.NO_REFERENCE, camReadout, null)
        }

        // ---- fingerprint ----
        val fp = signals.fingerprintSupported
        val expFp = expected?.fingerprint
        items += when {
            expFp == true && fp == false ->
                RefurbItem("fingerprint", RefurbStatus.MISMATCHED, "absent", "fp_missing")
            fp == true -> RefurbItem("fingerprint", RefurbStatus.NO_REFERENCE, "present", null)
            fp == false && expFp == null ->
                RefurbItem("fingerprint", RefurbStatus.NO_REFERENCE, "absent", null)
            else -> RefurbItem("fingerprint", RefurbStatus.UNREADABLE, null, null)
        }

        // ---- NFC expectation ----
        if (expected?.nfc == true) {
            items += when (signals.nfcPresent) {
                true -> RefurbItem("nfc", RefurbStatus.MATCHED, "present", null)
                false -> RefurbItem("nfc", RefurbStatus.MISMATCHED, "absent", "nfc_missing")
                null -> RefurbItem("nfc", RefurbStatus.UNREADABLE, null, null)
            }
        }

        // ---- presence readouts ----
        items += RefurbItem(
            "microphone", RefurbStatus.NO_REFERENCE,
            signals.microphone?.let { if (it) "present" else "absent" }, null
        )
        items += RefurbItem(
            "speaker", RefurbStatus.NO_REFERENCE,
            signals.speaker?.let { if (it) "present" else "absent" }, null
        )
        items += if (signals.vibratorCaps.isNullOrBlank()) {
            RefurbItem("vibrator", RefurbStatus.UNREADABLE, null, null)
        } else {
            RefurbItem("vibrator", RefurbStatus.NO_REFERENCE, signals.vibratorCaps, null)
        }

        val matched = items.count { it.status == RefurbStatus.MATCHED }
        val mismatched = items.count { it.status == RefurbStatus.MISMATCHED }
        val refTotal = matched + mismatched
        return RefurbReport(
            items = items,
            matchPercent = if (refTotal > 0) (matched * 100 / refTotal) else null,
            trustScore = trustScore(items),
            matchedCount = matched,
            mismatchedCount = mismatched
        )
    }

    /** Full-charge capacity estimate from the fuel gauge (labeled estimate). */
    fun estimateFullMah(remainingMah: Int?, chargePercent: Int?): Int? {
        if (remainingMah == null || chargePercent == null || chargePercent <= 0 || chargePercent > 100) {
            return null
        }
        return (remainingMah * 100 / chargePercent)
    }

    /** Advisory trust score: 100, −25 MISMATCHED, −8 UNREADABLE. Pure. */
    fun trustScore(items: List<RefurbItem>): Int {
        var s = 100
        items.forEach {
            when (it.status) {
                RefurbStatus.MISMATCHED -> s -= 25
                RefurbStatus.UNREADABLE -> s -= 8
                else -> Unit
            }
        }
        return s.coerceIn(0, 100)
    }
}
