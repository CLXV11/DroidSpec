package com.droidspec

import com.droidspec.domain.policy.DeviceReleaseEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceReleaseEstimatorTest {
    @Test
    fun knownCodenames() {
        assertEquals(2019, DeviceReleaseEstimator.estimate("ginkgo"))
        assertEquals(2017, DeviceReleaseEstimator.estimate("mido"))
    }

    @Test
    fun caseInsensitive() {
        assertEquals(2019, DeviceReleaseEstimator.estimate("GINKGO"))
    }

    @Test
    fun unknownReturnsNull() {
        assertNull(DeviceReleaseEstimator.estimate("mystery_device"))
        assertNull(DeviceReleaseEstimator.estimate(null))
    }
}
