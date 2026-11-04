package com.droidspec.domain.policy

/** Constants for the hardware test suite. Pure and unit-testable. */
object TestSpec {
    /** Dead-pixel test color sequence (ARGB). */
    val screenTestColors = listOf(
        0xFFFFFFFF, // white
        0xFF000000, // black
        0xFFFF0000, // red
        0xFF00FF00, // green
        0xFF0000FF  // blue
    )

    const val FPS_MEASURE_MS = 2000L
    const val VIBRATE_MS = 500L
    const val TONE_DURATION_MS = 1000
}
