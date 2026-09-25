package com.droidspec

import com.droidspec.domain.model.RefurbItem
import com.droidspec.domain.model.RefurbStatus
import com.droidspec.domain.policy.ExpectedSpec
import com.droidspec.domain.policy.ExpectedComponents
import com.droidspec.domain.policy.RefurbAnalyzer
import com.droidspec.domain.policy.RefurbSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefurbAnalyzerTest {
    private fun base() = RefurbSignals(
        touchControllers = listOf("fts_ts (vid:0386 pid:0db2)"),
        panelInfo = "tianma fhd video mode dsi panel",
        batteryCycles = 120,
        batteryHealthKey = "good",
        capacityPercent = 92,
        physicalSizeInches = 6.4,
        cameraCount = 4,
        usbState = "USB idle",
        fingerprintSupported = true,
        microphone = true,
        speaker = true,
        vibrator = true,
        chargerType = "USB",
        nfcPresent = false
    )

    @Test
    fun allHealthyIsGenuineWithHighScore() {
        val r = RefurbAnalyzer.analyze(base(), ExpectedSpec(4, true, false))
        assertEquals(RefurbStatus.GENUINE_LIKELY, r.overall)
        assertTrue(r.score >= 80)
        assertEquals(0, r.suspiciousCount)
    }

    @Test
    fun deadBatteryIsSuspicious() {
        val r = RefurbAnalyzer.analyze(base().copy(batteryHealthKey = "dead", capacityPercent = null))
        assertEquals(RefurbStatus.SUSPICIOUS, r.overall)
        assertTrue(r.score <= 75)
    }

    @Test
    fun highCycleCountIsSuspicious() {
        assertEquals(RefurbStatus.SUSPICIOUS, RefurbAnalyzer.analyze(base().copy(batteryCycles = 1200)).overall)
    }

    @Test
    fun degradedCapacityIsSuspicious() {
        assertEquals(RefurbStatus.SUSPICIOUS, RefurbAnalyzer.analyze(base().copy(capacityPercent = 61)).overall)
    }

    @Test
    fun implausibleScreenSizeIsSuspicious() {
        assertEquals(RefurbStatus.SUSPICIOUS, RefurbAnalyzer.analyze(base().copy(physicalSizeInches = 0.4)).overall)
    }

    @Test
    fun zeroCamerasIsSuspicious() {
        assertEquals(RefurbStatus.SUSPICIOUS, RefurbAnalyzer.analyze(base().copy(cameraCount = 0)).overall)
    }

    @Test
    fun cameraMismatchWithExpectedDbIsSuspicious() {
        val r = RefurbAnalyzer.analyze(base().copy(cameraCount = 2), ExpectedSpec(4, true, null))
        val cam = r.items.first { it.componentKey == "camera" }
        assertEquals(RefurbStatus.SUSPICIOUS, cam.status)
        assertEquals("exp=4 got=2", cam.detail)
    }

    @Test
    fun missingExpectedFingerprintIsSuspicious() {
        val r = RefurbAnalyzer.analyze(base().copy(fingerprintSupported = false), ExpectedSpec(4, true, null))
        assertEquals(RefurbStatus.SUSPICIOUS, r.overall)
    }

    @Test
    fun missingExpectedNfcIsSuspicious() {
        val r = RefurbAnalyzer.analyze(base(), ExpectedSpec(4, true, true))
        val nfc = r.items.first { it.componentKey == "nfc" }
        assertEquals(RefurbStatus.SUSPICIOUS, nfc.status)
    }

    @Test
    fun mostlyUnknownYieldsLowScore() {
        val r = RefurbAnalyzer.analyze(
            base().copy(
                touchControllers = emptyList(), panelInfo = null, usbState = null,
                physicalSizeInches = null, chargerType = null, vibrator = null
            )
        )
        assertTrue(r.unknownCount >= 4)
        assertTrue(r.score < 80)
    }

    @Test
    fun scoreMathIsPure() {
        val ok = RefurbItem("a", RefurbStatus.GENUINE_LIKELY, null)
        val bad = RefurbItem("b", RefurbStatus.SUSPICIOUS, null)
        val unk = RefurbItem("c", RefurbStatus.UNKNOWN, null)
        assertEquals(100, RefurbAnalyzer.score(listOf(ok)))
        assertEquals(75, RefurbAnalyzer.score(listOf(ok, bad)))
        assertEquals(65, RefurbAnalyzer.score(listOf(ok, bad, unk)))
        assertEquals(0, RefurbAnalyzer.score(List(5) { bad }))
    }

    @Test
    fun expectedDbLookup() {
        assertEquals(4, ExpectedComponents.forDevice("GINKGO")?.cameras)
        assertNull(ExpectedComponents.forDevice("unknown_device"))
        assertNull(ExpectedComponents.forDevice(null))
    }
}
