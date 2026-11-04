package com.droidspec.presentation.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidspec.R
import com.droidspec.domain.benchmark.AdvancedBench
import com.droidspec.domain.benchmark.GpuBench
import com.droidspec.domain.policy.TestSpec
import com.droidspec.ui.components.NoteText
import com.droidspec.ui.components.PrimaryButton
import com.droidspec.ui.components.SpecCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

private enum class ActiveTest { SCREEN, TOUCH, NONE }

@Composable
fun HardwareTestsScreen() {
    var active by remember { mutableStateOf(ActiveTest.NONE) }
    when (active) {
        ActiveTest.SCREEN -> ScreenTest { active = ActiveTest.NONE }
        ActiveTest.TOUCH -> TouchTest { active = ActiveTest.NONE }
        ActiveTest.NONE -> TestsMenu { active = it }
    }
}

@Composable
private fun TestsMenu(onActive: (ActiveTest) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SpecCard(stringResource(R.string.test_screen)) {
            Text(stringResource(R.string.test_screen_desc), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(onClick = { onActive(ActiveTest.SCREEN) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.test_start))
            }
        }
        SpecCard(stringResource(R.string.test_touch)) {
            Text(stringResource(R.string.test_touch_desc), style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(onClick = { onActive(ActiveTest.TOUCH) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.test_start))
            }
        }
        VibrationCard()
        ToneCard()
        FpsMeterCard()
        GpuBenchCard()
        MemoryLatencyCard()
        StorageIopsCard()
        ThrottlingCard()
        NoteText(stringResource(R.string.test_note))
    }
}

// ------------------------------ Screen test -----------------------------

@Composable
private fun ScreenTest(onExit: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    val colors = TestSpec.screenTestColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(colors[index.coerceAtMost(colors.lastIndex)]))
            .clickable { if (index < colors.lastIndex) index++ else onExit() }
    ) {
        val fg = if (index == 1) Color.White else Color.Black
        Text(
            text = stringResource(R.string.test_tap_next, index + 1, colors.size),
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
        )
        TextButton(onClick = onExit, modifier = Modifier.align(Alignment.TopEnd)) {
            Text(stringResource(R.string.test_exit), color = fg)
        }
    }
}

// ------------------------------ Touch test ------------------------------

@Composable
private fun TouchTest(onExit: () -> Unit) {
    val positions = remember { mutableStateMapOf<Long, Offset>() }
    var maxPointers by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101318))
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                object : android.view.View(ctx) {
                    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
                        positions.clear()
                        for (i in 0 until event.pointerCount) {
                            positions[event.getPointerId(i).toLong()] =
                                Offset(event.getX(i), event.getY(i))
                        }
                        if (event.pointerCount > maxPointers) maxPointers = event.pointerCount
                        return true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            positions.values.forEach { pos ->
                drawCircle(Color(0xFF81D5C6), radius = 60f, center = pos, alpha = 0.35f)
                drawCircle(Color(0xFF81D5C6), radius = 14f, center = pos)
            }
        }
        Text(
            text = stringResource(R.string.test_touch_count, positions.size, maxPointers),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(24.dp)
        )
        TextButton(onClick = onExit, modifier = Modifier.align(Alignment.TopEnd)) {
            Text(stringResource(R.string.test_exit), color = Color.White)
        }
    }
}

// ---------------------------- Vibration & tone ----------------------------

@SuppressLint("MissingPermission")
@Composable
private fun VibrationCard() {
    val context = LocalContext.current
    SpecCard(stringResource(R.string.test_vibrate)) {
        Text(stringResource(R.string.test_vibrate_desc), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(onClick = { vibrate(context) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.test_vibrate_run))
        }
    }
}

@SuppressLint("MissingPermission")
private fun vibrate(context: Context) {
    runCatching {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(TestSpec.VIBRATE_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(TestSpec.VIBRATE_MS)
        }
    }
}

@Composable
private fun ToneCard() {
    SpecCard(stringResource(R.string.test_tone)) {
        Text(stringResource(R.string.test_tone_desc), style = MaterialTheme.typography.bodyMedium)
        PrimaryButton(onClick = { playTone() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.test_tone_run))
        }
    }
}

private fun playTone() {
    runCatching {
        kotlin.concurrent.thread(name = "droidspec-tone") {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            try {
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, TestSpec.TONE_DURATION_MS)
                Thread.sleep((TestSpec.TONE_DURATION_MS + 200).toLong())
            } finally {
                tg.release()
            }
        }
    }
}

// ------------------------------ FPS meter -------------------------------

@Composable
private fun FpsMeterCard() {
    val context = LocalContext.current
    val nominal = remember {
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            @Suppress("DEPRECATION")
            wm.defaultDisplay.refreshRate
        }.getOrNull()
    }
    var fps by remember { mutableStateOf<Float?>(null) }
    var measuring by remember { mutableStateOf(false) }

    LaunchedEffect(measuring) {
        if (!measuring) return@LaunchedEffect
        // Compose's MonotonicFrameClock suspends until each real frame is
        // presented — the same signal Choreographer drives, minus the import.
        var frames = 0
        val start = System.nanoTime()
        while (System.nanoTime() - start < TestSpec.FPS_MEASURE_MS * 1_000_000L) {
            withFrameNanos { }
            frames++
        }
        fps = frames * 1_000_000_000f / (System.nanoTime() - start)
        measuring = false
    }

    SpecCard(stringResource(R.string.test_fps)) {
        Text(stringResource(R.string.test_fps_desc), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = { measuring = true; fps = null },
                enabled = !measuring
            ) { Text(stringResource(R.string.test_fps_run)) }
            Spacer(Modifier.size(12.dp))
            val f = fps
            when {
                f != null -> Text(
                    text = stringResource(R.string.test_fps_result, f) +
                        (nominal?.let { n -> " / ${n.toInt()} Hz" } ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                measuring -> Text(stringResource(R.string.common_loading))
            }
        }
    }
}

// --------------------------- Advanced benches ----------------------------

@Composable
private fun GpuBenchCard() {
    var result by remember { mutableStateOf<GpuBench.Result?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SpecCard(stringResource(R.string.test_gpu)) {
        Text(stringResource(R.string.test_gpu_desc), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = {
                    running = true; result = null
                    scope.launch(Dispatchers.Default) {
                        result = GpuBench.run(); running = false
                    }
                },
                enabled = !running
            ) { Text(stringResource(R.string.test_gpu_run)) }
            Spacer(Modifier.size(12.dp))
            val r = result
            when {
                r != null -> Text(
                    text = stringResource(R.string.test_gpu_result, r.fps, r.trianglesPerFrame),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                running -> Text(stringResource(R.string.common_loading))
            }
        }
    }
}

@Composable
private fun MemoryLatencyCard() {
    var ns by remember { mutableStateOf<Double?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SpecCard(stringResource(R.string.test_memlat)) {
        Text(stringResource(R.string.test_memlat_desc), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = {
                    running = true; ns = null
                    scope.launch(Dispatchers.Default) {
                        ns = AdvancedBench.memoryLatencyNs(); running = false
                    }
                },
                enabled = !running
            ) { Text(stringResource(R.string.test_memlat_run)) }
            Spacer(Modifier.size(12.dp))
            val v = ns
            when {
                v != null -> Text(
                    text = stringResource(R.string.test_memlat_result, v),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                running -> Text(stringResource(R.string.common_loading))
            }
        }
    }
}

@Composable
private fun StorageIopsCard() {
    val context = LocalContext.current
    var iops by remember { mutableStateOf<AdvancedBench.IopsResult?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SpecCard(stringResource(R.string.test_iops)) {
        Text(stringResource(R.string.test_iops_desc), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = {
                    running = true; iops = null
                    scope.launch(Dispatchers.IO) {
                        iops = AdvancedBench.storageIops(context.cacheDir); running = false
                    }
                },
                enabled = !running
            ) { Text(stringResource(R.string.test_iops_run)) }
            Spacer(Modifier.size(12.dp))
            val r = iops
            when {
                r != null -> Text(
                    text = stringResource(R.string.test_iops_result, r.writeIops, r.readIops),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                running -> Text(stringResource(R.string.common_loading))
            }
        }
    }
}

private fun currentCpuFreqMhz(): Double? = runCatching {
    val f = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
    if (!f.canRead()) return null
    f.readText().trim().toDoubleOrNull()?.let { it / 1000.0 }
}.getOrNull()

private suspend fun cpuChunk400ms() {
    kotlinx.coroutines.withContext(Dispatchers.Default) {
        val end = System.currentTimeMillis() + 400
        var x = 1L
        while (System.currentTimeMillis() < end) {
            repeat(50_000) {
                x = x * 6364136223846793005L + 1442695040888963407L
                x = x xor (x shr 33)
            }
            currentCoroutineContext().ensureActive()
        }
        if (x == 0L) println("unreachable")
    }
}

@Composable
private fun ThrottlingCard() {
    var result by remember { mutableStateOf<AdvancedBench.ThrottleResult?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SpecCard(stringResource(R.string.test_throttle)) {
        Text(stringResource(R.string.test_throttle_desc), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(
                onClick = {
                    running = true; result = null
                    scope.launch(Dispatchers.Default) {
                        result = AdvancedBench.throttling(
                            cpuChunk = { cpuChunk400ms() },
                            freqMhz = { currentCpuFreqMhz() }
                        )
                        running = false
                    }
                },
                enabled = !running
            ) { Text(stringResource(R.string.test_throttle_run)) }
            Spacer(Modifier.size(12.dp))
            val r = result
            when {
                r == null && running -> Text(stringResource(R.string.common_loading))
                r != null && r.dropPercent != null -> Text(
                    text = stringResource(
                        R.string.test_throttle_result,
                        r.startMhz ?: 0.0, r.minMhz ?: 0.0, r.dropPercent
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (r.dropPercent >= 15) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
                r != null -> Text(stringResource(R.string.test_throttle_na))
            }
        }
    }
}
