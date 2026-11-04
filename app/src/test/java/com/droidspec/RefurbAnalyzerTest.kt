package com.droidspec

import com.droidspec.domain.model.RefurbStatus
import com.droidspec.domain.policy.ExpectedComponents
import com.droidspec.domain.policy.ExpectedSpec
import com.droidspec.domain.policy.HardwareIdParsers
import com.droidspec.domain.policy.RefurbAnalyzer
import com.droidspec.domain.policy.RefurbSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefurbAnalyzerTest {
    private fun base() = RefurbSignals(
        touchControllers = listOf("fts_ts"),
        panelVendor = "Tianma",
        panelRaw = "tianma fhd panel",
        batteryCycles = 120,
        batteryHealthKey = "good",
        remainingMah = 2000,
        chargePercent = 50,
        batteryVoltageMv = 3870,
        batteryTempC = 32.5f,
        cameraCount = 4,
        cameraOisSupported = false,
        usbState = "USB idle",
        chargerType = "USB",
        fingerprintSupported = true,
        microphone = true,
        speaker = true,
        vibratorCaps = "amplitude=yes",
        nfcPresent = false,
        physicalSizeInches = 6.3
    )

    private fun exp() = ExpectedSpec("Snapdragon 665", 4, true, false, 4000, null, null)

    @Test
    fun fullChargeEstimateMath() {
        assertEquals(4000, RefurbAnalyzer.estimateFullMah(2000, 50))
        assertEquals(4000, RefurbAnalyzer.estimateFullMah(4000, 100))
        assertNull(RefurbAnalyzer.estimateFullMah(null, 50))
        assertNull(RefurbAnalyzer.estimateFullMah(100, 0))
    }

    @Test
    fun healthyBatteryNotFlaggedAtHalfCharge() {
        // 50% remaining with full capacity ≈ design is NOT wear
        val r = RefurbAnalyzer.analyze(base(), exp())
        val batt = r.items.first { it.componentKey == "battery" }
        assertTrue(batt.status != RefurbStatus.MISMATCHED)
    }

    @Test
    fun wornBatteryFlagged() {
        val r = RefurbAnalyzer.analyze(
            base().copy(batteryCycles = 900), exp()
        )
        assertEquals(RefurbStatus.MISMATCHED, r.items.first { it.componentKey == "battery" }.status)
    }

    @Test
    fun degradedFullCapacityFlagged() {
        val r = RefurbAnalyzer.analyze(
            base().copy(remainingMah = 1500, chargePercent = 50), exp() // full≈3000 < 80% of 4000
        )
        assertEquals(RefurbStatus.MISMATCHED, r.items.first { it.componentKey == "battery" }.status)
    }

    @Test
    fun cameraCountMismatch() {
        val r = RefurbAnalyzer.analyze(base().copy(cameraCount = 2), exp())
        val cam = r.items.first { it.componentKey == "camera" }
        assertEquals(RefurbStatus.MISMATCHED, cam.status)
        assertEquals("camera_count", cam.noteKey)
    }

    @Test
    fun missingFingerprintMismatch() {
        val r = RefurbAnalyzer.analyze(base().copy(fingerprintSupported = false), exp())
        assertEquals(RefurbStatus.MISMATCHED, r.items.first { it.componentKey == "fingerprint" }.status)
    }

    @Test
    fun nfcExpectation() {
        val r = RefurbAnalyzer.analyze(base(), exp().copy(nfc = true))
        assertEquals(RefurbStatus.MISMATCHED, r.items.first { it.componentKey == "nfc" }.status)
        val r2 = RefurbAnalyzer.analyze(base().copy(nfcPresent = true), exp().copy(nfc = true))
        assertEquals(RefurbStatus.MATCHED, r2.items.first { it.componentKey == "nfc" }.status)
    }

    @Test
    fun matchPercentMath() {
        val r = RefurbAnalyzer.analyze(base(), exp()) // nfc not expected -> only ref checks pass
        assertTrue(r.trustScore > 60)
    }

    @Test
    fun panelVendorMismatchWhenReferenced() {
        val r = RefurbAnalyzer.analyze(
            base(), exp().copy(panelVendors = listOf("Samsung"))
        )
        assertEquals(RefurbStatus.MISMATCHED, r.items.first { it.componentKey == "panel" }.status)
    }

    @Test
    fun parsers() {
        assertEquals("Tianma", HardwareIdParsers.identifyVendor("tianma_fhd_video"))
        assertEquals("Samsung", HardwareIdParsers.identifyVendor("s6e8fc1x01"))
        assertNull(HardwareIdParsers.identifyVendor("unknown panel"))
        val names = HardwareIdParsers.parseTouchDevices(
            "I: Bus=0018 Vendor=0000 Product=0000\nN: Name=\"fts_ts\"\nN: Name=\"gpio-keys\""
        )
        assertEquals(listOf("fts_ts"), names)
    }

    @Test
    fun expectedDb() {
        assertEquals(4000, ExpectedComponents.forDevice("ginkgo")?.batteryDesignMah)
        assertNull(ExpectedComponents.forDevice("mystery"))
    }
}
