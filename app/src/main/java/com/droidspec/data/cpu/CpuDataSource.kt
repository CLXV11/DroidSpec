package com.droidspec.data.cpu

import android.os.Build
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.model.CoreInfo
import com.droidspec.domain.model.CpuInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * CPU information from /proc and (best-effort) kernel cpufreq nodes.
 * Nothing is fabricated: anything the kernel does not expose stays null.
 */
class CpuDataSource {

    private val cpuRoot = File("/sys/devices/system/cpu")

    fun snapshotStatic(): CpuInfo {
        val name = readCpuName()
        val cores = Runtime.getRuntime().availableProcessors()
        val perCore = (0 until cores).map { index ->
            CoreInfo(
                index = index,
                currentMhz = readKhz(cpuDir(index, "cpufreq/scaling_cur_freq"))
                    ?: readKhz(cpuDir(index, "cpufreq/cpuinfo_cur_freq")),
                maxMhz = CalcUtils.kHzToMhz(readKhzRaw(cpuDir(index, "cpufreq/cpuinfo_max_freq")))
            )
        }
        return CpuInfo(
            name = name,
            architecture = Build.SUPPORTED_ABIS.firstOrNull(),
            abi = Build.SUPPORTED_ABIS.firstOrNull(),
            coreCount = cores,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            currentFreqMhz = readKhz(File(cpuRoot, "cpu0/cpufreq/scaling_cur_freq"))
                ?: readKhz(File(cpuRoot, "cpu0/cpufreq/cpuinfo_cur_freq")),
            minFreqMhz = readKhz(File(cpuRoot, "cpu0/cpufreq/cpuinfo_min_freq")),
            maxFreqMhz = readKhz(File(cpuRoot, "cpu0/cpufreq/cpuinfo_max_freq")),
            perCore = perCore,
            usagePercent = null // usage is filled asynchronously by measureUsage()
        )
    }

    suspend fun measureUsage(): Double? = withContext(Dispatchers.IO) {
        val first = readStat() ?: return@withContext null
        delay(150)
        val second = readStat() ?: return@withContext null
        val totalDelta = (second.total - first.total).toDouble()
        if (totalDelta <= 0.0) return@withContext null
        val idleDelta = (second.idle - first.idle).toDouble()
        ((totalDelta - idleDelta) / totalDelta * 100.0).coerceIn(0.0, 100.0)
    }

    private fun readCpuName(): String? = runCatching {
        File("/proc/cpuinfo").readLines().firstNotNullOfOrNull { line ->
            when {
                line.startsWith("model name", ignoreCase = true) -> line.substringAfter(':').trim()
                line.startsWith("Hardware", ignoreCase = true) -> line.substringAfter(':').trim()
                else -> null
            }
        }?.ifBlank { null }
    }.getOrNull()

    private fun cpuDir(index: Int, child: String) = File(cpuRoot, "cpu$index/$child")

    private fun readKhzRaw(file: File): Long? = runCatching {
        if (!file.canRead()) return null
        file.readText().trim().toLongOrNull()
    }.getOrNull()

    private fun readKhz(file: File): Double? = CalcUtils.kHzToMhz(readKhzRaw(file))

    private class Stat(val idle: Long, val total: Long)

    private fun readStat(): Stat? = runCatching {
        val parts = File("/proc/stat").readLines().first { it.startsWith("cpu ") }
            .split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
        if (parts.size < 8) return null
        val idle = parts[3] + parts[4]
        val total = parts.subList(0, 8).sum()
        Stat(idle, total)
    }.getOrNull()
}
