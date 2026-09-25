package com.droidspec

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Working directory for unit tests is the app module dir, so both resource
 * files are readable directly from the filesystem.
 */
class LocalizationTest {
    @Test
    fun arabicAndEnglishKeyParity() {
        val en = File("src/main/res/values/strings.xml").readText()
        val ar = File("src/main/res/values-ar/strings.xml").readText()
        fun keys(xml: String) =
            Regex("<string name=\"([^\"]+)\"").findAll(xml).map { it.groupValues[1] }.toSet()
        assertEquals("EN/AR string key mismatch", keys(en), keys(ar))
    }
}
