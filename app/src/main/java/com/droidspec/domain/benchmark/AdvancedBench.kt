package com.droidspec.domain.benchmark

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.RandomAccessFile
import java.util.Random

/**
 * Advanced internal benchmarks: memory latency (pointer-chase that defeats
 * the cache prefetcher), storage 4K random I/O with real fsync, and a
 * thermal-throttling probe. All bounded and cancellable.
 */
object AdvancedBench {

    /**
     * Random pointer-chase across an 8 MB linked list — the classic trick to
     * measure TRUE memory latency: the CPU cannot prefetch a random chase.
     * Returns nanoseconds per access.
     */
    fun memoryLatencyNs(): Double {
        val size = 2_000_000
        val links = IntArray(size)
        val order = IntArray(size) { it }
        val rnd = Random(42)
        for (i in size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val t = order[i]; order[i] = order[j]; order[j] = t
        }
        for (i in 0 until size - 1) links[order[i]] = order[i + 1]
        links[order[size - 1]] = order[0]

        var idx = 0
        repeat(200_000) { idx = links[idx] } // warm-up chases
        val steps = 2_000_000
        val start = System.nanoTime()
        repeat(steps) { idx = links[idx] }
        val ns = (System.nanoTime() - start).toDouble() / steps
        if (idx == -987_654) println("unreachable") // keep idx live
        return ns
    }

    data class IopsResult(val writeIops: Double, val readIops: Double)

    /** Random 4K writes (with fsync) then reads, for [seconds] each. */
    fun storageIops(cacheDir: File, seconds: Int = 2): IopsResult? = runCatching {
        val f = File(cacheDir, "droidspec_iops.tmp")
        val raf = RandomAccessFile(f, "rw")
        try {
            raf.setLength(32L * 1024 * 1024)
            val block = ByteArray(4096)
            Random(7).nextBytes(block)
            val buf = ByteArray(4096)
            val rnd = Random(99)
            val maxBlock = (32L * 1024 * 1024 / 4096).toInt()

            val wStart = System.nanoTime()
            var wOps = 0
            while (System.nanoTime() - wStart < seconds * 1_000_000_000L) {
                raf.seek(rnd.nextInt(maxBlock) * 4096L)
                raf.write(block)
                raf.fd.sync()
                wOps++
            }
            val rStart = System.nanoTime()
            var rOps = 0
            while (System.nanoTime() - rStart < seconds * 1_000_000_000L) {
                raf.seek(rnd.nextInt(maxBlock) * 4096L)
                raf.readFully(buf)
                rOps++
            }
            IopsResult(wOps.toDouble() / seconds, rOps.toDouble() / seconds)
        } finally {
            raf.close()
            f.delete()
        }
    }.getOrNull()

    data class ThrottleResult(
        val startMhz: Double?,
        val minMhz: Double?,
        val dropPercent: Int?
    )

    /**
     * Sustains ~8 s of CPU load, sampling the current frequency after each
     * chunk. A healthy cool chip keeps its frequency; a throttling one drops.
     */
    suspend fun throttling(
        cpuChunk: suspend () -> Unit,
        freqMhz: () -> Double?
    ): ThrottleResult {
        val start = freqMhz()
        val samples = mutableListOf<Double>()
        val t0 = System.currentTimeMillis()
        while (System.currentTimeMillis() - t0 < 8000) {
            currentCoroutineContext().ensureActive()
            cpuChunk()
            freqMhz()?.let { samples.add(it) }
        }
        val min = samples.minOrNull()
        return if (start != null && min != null && start > 0) {
            val drop = (((start - min).coerceAtLeast(0.0)) * 100 / start).toInt()
            ThrottleResult(start, min, drop)
        } else {
            ThrottleResult(start, min, null)
        }
    }
}
