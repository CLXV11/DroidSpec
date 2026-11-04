package com.droidspec.domain.benchmark

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min

/**
 * DroidSpec Internal Benchmark. Short, bounded, cancellable workloads.
 * The score is internal-only and NOT comparable to commercial benchmarks.
 */
class BenchmarkEngine(private val context: Context) {

    enum class Stage { CPU, MEMORY, STORAGE }

    sealed interface BenchState {
        data object Idle : BenchState
        data class Running(val stage: Stage, val progressPercent: Int) : BenchState
        data class Done(val result: BenchmarkResult) : BenchState
        data object Cancelled : BenchState
    }

    data class BenchmarkResult(
        val cpuOpsPerSec: Long,
        val memoryMbps: Double,
        val storageMbps: Double,
        val internalScore: Int,
        val durationMs: Long
    )

    private var job: kotlinx.coroutines.Job? = null

    fun cancel() {
        job?.cancel()
    }

    fun run(scope: CoroutineScope, onState: (BenchState) -> Unit) {
        if (job?.isActive == true) return
        job = scope.launch(Dispatchers.Default) {
            val totalStart = System.currentTimeMillis()
            try {
                onState(BenchState.Running(Stage.CPU, 0))
                val cpu = cpuStage { pct -> onState(BenchState.Running(Stage.CPU, pct)) }
                val mem = memoryStage { pct -> onState(BenchState.Running(Stage.MEMORY, pct)) }
                val storage = storageStage { pct -> onState(BenchState.Running(Stage.STORAGE, pct)) }
                val score = internalScore(cpu, mem, storage)
                onState(
                    BenchState.Done(
                        BenchmarkResult(
                            cpuOpsPerSec = cpu,
                            memoryMbps = mem,
                            storageMbps = storage,
                            internalScore = score,
                            durationMs = System.currentTimeMillis() - totalStart
                        )
                    )
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                onState(BenchState.Cancelled)
                throw e
            }
        }
    }

    /** ~1.5 s of integer work after a JIT warm-up; checks cancellation every chunk. */
    private suspend fun cpuStage(report: (Int) -> Unit): Long {
        val targetMs = 1500L
        var ops = 0L
        var x = 123456789L
        // Warm-up: get the JIT hot before measuring, for stable scores
        val warmStart = System.currentTimeMillis()
        while (System.currentTimeMillis() - warmStart < 300) {
            repeat(200_000) {
                x = x * 6364136223846793005L + 1442695040888963407L
                x = x xor (x shr 33)
            }
            currentCoroutineContext().ensureActive()
        }
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < targetMs) {
            repeat(200_000) {
                x = x * 6364136223846793005L + 1442695040888963407L
                x = x xor (x shr 33)
            }
            ops += 200_000
            currentCoroutineContext().ensureActive()
            report(min(99, ((System.currentTimeMillis() - start) * 100 / targetMs).toInt()))
        }
        if (x == Long.MIN_VALUE) ops += 0 // keep x used
        report(100)
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1)
        return ops * 1000 / elapsed
    }

    /** ~1.5 s of array copy; bounded allocations. */
    private suspend fun memoryStage(report: (Int) -> Unit): Double {
        val targetMs = 1500L
        val block = ByteArray(4 * 1024 * 1024)
        val dst = ByteArray(4 * 1024 * 1024)
        var copied = 0L
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < targetMs) {
            System.arraycopy(block, 0, dst, 0, block.size)
            copied += block.size
            currentCoroutineContext().ensureActive()
            report(min(99, ((System.currentTimeMillis() - start) * 100 / targetMs).toInt()))
        }
        report(100)
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1)
        return copied / 1024.0 / 1024.0 / (elapsed / 1000.0)
    }

    /** ~1.5 s write+read of a temp file inside the app cache dir. */
    private suspend fun storageStage(report: (Int) -> Unit): Double = withContext(Dispatchers.IO) {
        val targetMs = 1500L
        val file = File(context.cacheDir, "droidspec_bench.tmp")
        val data = ByteArray(1024 * 1024)
        var total = 0L
        val start = System.currentTimeMillis()
        file.outputStream().buffered(1024 * 1024).use { out ->
            while (System.currentTimeMillis() - start < targetMs) {
                out.write(data)
                total += data.size
                currentCoroutineContext().ensureActive()
                report(min(99, ((System.currentTimeMillis() - start) * 100 / targetMs).toInt()))
            }
        }
        file.inputStream().buffered(1024 * 1024).use { input ->
            val buf = ByteArray(64 * 1024)
            val readStart = System.currentTimeMillis()
            while (System.currentTimeMillis() - readStart < 500L) {
                if (input.read(buf) == -1) break
                currentCoroutineContext().ensureActive()
            }
        }
        file.delete()
        report(100)
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1)
        total / 1024.0 / 1024.0 / (elapsed / 1000.0)
    }

    companion object {
        /** Deterministic internal scoring. Pure — callable without an instance. */
        fun internalScore(cpuOpsPerSec: Long, memoryMbps: Double, storageMbps: Double): Int {
            val cpuComponent = (cpuOpsPerSec / 20_000_000.0) * 40.0
            val memComponent = (memoryMbps / 4000.0) * 30.0
            val storageComponent = (storageMbps / 400.0) * 30.0
            return (cpuComponent + memComponent + storageComponent)
                .coerceIn(0.0, 1000.0).toInt()
        }
    }
}
