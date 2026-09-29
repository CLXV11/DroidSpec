package com.droidspec.domain.search

import com.droidspec.domain.usecase.SectionSnapshotUseCases

data class SearchEntry(
    val sectionKey: String,
    val title: String,
    val value: String
)

/**
 * Offline in-memory search index built once from real snapshots.
 * Pure filtering over a list — trivially unit-testable.
 */
class SearchEngine(private val snapshots: SectionSnapshotUseCases) {

    suspend fun buildIndex(): List<SearchEntry> {
        val entries = mutableListOf<SearchEntry>()
        fun add(section: String, title: String, value: String?) {
            if (!value.isNullOrBlank()) entries += SearchEntry(section, title, value)
        }

        val device = snapshots.device()
        add("device", "Manufacturer", device.manufacturer)
        add("device", "Brand", device.brand)
        add("device", "Model", device.model)
        add("device", "Android version", device.androidVersion)
        add("device", "Security patch", device.securityPatch)
        add("device", "Kernel", device.kernelVersion)
        add("device", "Architecture", device.architecture)
        add("device", "ABIs", device.supportedAbis.joinToString(", ").ifBlank { null })

        val cpu = snapshots.cpu()
        add("cpu", "CPU", cpu.name)
        add("cpu", "Cores", cpu.coreCount.takeIf { it > 0 }?.toString())
        add("cpu", "ABI", cpu.abi)

        val gpu = snapshots.gpu()
        add("gpu", "GPU renderer", gpu.renderer)
        add("gpu", "GPU vendor", gpu.vendor)
        add("gpu", "OpenGL ES", gpu.glesVersion)
        add("gpu", "Vulkan", gpu.vulkan)

        val ram = snapshots.ram()
        add("memory", "Total RAM", ram.totalBytes.takeIf { it > 0 }?.toString())
        val storage = snapshots.storage()
        add("storage", "Total storage", storage.totalBytes.takeIf { it > 0 }?.toString())

        val battery = snapshots.battery()
        add("battery", "Battery technology", battery.technology)
        add("battery", "Battery health", battery.healthKey)
        add("battery", "Charging source", battery.sourceKey)

        val display = snapshots.display()
        add("display", "Resolution", display.widthPx?.let { "${it}x${display.heightPx}" })
        add("display", "Refresh rate", display.refreshRateHz?.toString())
        add("display", "DPI", display.densityDpi?.toString())

        val camera = snapshots.camera()
        camera.cameras.forEach { cam ->
            add("camera", "Camera ${cam.id}", cam.facingKey)
            cam.megapixelsCalculated?.let { add("camera", "Camera ${cam.id} resolution", "%.1f MP".format(it)) }
        }

        snapshots.sensorList().forEach { s -> add("sensors", s.name, s.vendor ?: "available") }

        val network = snapshots.network()
        add("network", "Transport", network.transportKey)
        return entries
    }

    fun filter(index: List<SearchEntry>, query: String, limit: Int = 50): List<SearchEntry> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        return index.filter {
            it.title.lowercase().contains(q) || it.value.lowercase().contains(q)
        }.take(limit)
    }
}
