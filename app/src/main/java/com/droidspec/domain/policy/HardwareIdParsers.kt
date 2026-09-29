package com.droidspec.domain.policy

/**
 * Pure parsers that turn raw kernel strings (panel info, input device names)
 * into human-readable hardware identities. No Android dependencies —
 * fully unit-testable.
 */
object HardwareIdParsers {

    private val panelSignatures: List<Pair<String, String>> = listOf(
        "s6e" to "Samsung",
        "samsung" to "Samsung",
        "tianma" to "Tianma",
        "boe" to "BOE",
        "csot" to "CSOT",
        "auo" to "AUO",
        "sharp" to "Sharp",
        "lgd" to "LG Display",
        "lg" to "LG Display",
        "hx83" to "Himax",
        "nt36" to "Novatek",
        "ft871" to "Focaltech",
        "focaltech" to "Focaltech",
        "fts" to "Focaltech",
        "goodix" to "Goodix",
        "synaptics" to "Synaptics",
        "jdi" to "JDI"
    )

    /** Identify a panel/controller vendor from a raw kernel string. */
    fun identifyVendor(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val n = raw.lowercase()
        return panelSignatures.firstOrNull { n.contains(it.first) }?.second
    }

    /**
     * Parse /proc/bus/input/devices and return the names of likely
     * touchscreen devices (kernel driver names are how replaced panels
     * betray themselves).
     */
    fun parseTouchDevices(procInput: String): List<String> {
        val names = mutableListOf<String>()
        for (line in procInput.lines()) {
            if (!line.startsWith("N: Name=")) continue
            val name = line.substringAfter("N: Name=").trim().trim('"')
            val l = name.lowercase()
            if (l.contains("touch") || l.contains("_ts") || l.contains("ts_") ||
                l.contains("goodix") || l.contains("focaltech") || l.contains("synaptics")
            ) {
                names.add(name)
            }
        }
        return names.distinct()
    }

    /** Best-effort: pull every readable fb0 panel node into one blob. */
    fun panelBlob(read: (String) -> String?): String {
        val nodes = listOf(
            "/sys/class/graphics/fb0/msm_fb_panel_info",
            "/sys/class/graphics/fb0/panel_info",
            "/sys/class/graphics/fb0/msm_fb_panel_name",
            "/sys/class/graphics/fb0/name"
        )
        return nodes.mapNotNull { read(it)?.lineSequence()?.firstOrNull() }
            .joinToString(" | ")
    }
}
