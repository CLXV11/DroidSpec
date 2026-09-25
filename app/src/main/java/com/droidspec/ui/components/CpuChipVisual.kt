package com.droidspec.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.domain.policy.CpuBrand

/**
 * Stylized processor package illustration drawn with Canvas.
 * Decorative graphic — NOT a photograph of the user's actual silicon.
 * The overlaid text (brand / real cpuinfo name / real max frequency) is data.
 */
@Composable
fun CpuChipVisual(
    brand: CpuBrand,
    cpuName: String?,
    coreCount: Int,
    maxFreqMhz: Double?,
    modifier: Modifier = Modifier
) {
    val usage = 0f

    val bodyColor = Color(0xFF8BC34A)   // light green package
    val borderColor = Color(0xFF33691E)
    val pinColor = Color(0xFF558B2F)
    val dieColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val bodyW = w * 0.6f
            val bodyH = h * 0.7f
            val left = (w - bodyW) / 2f
            val top = (h - bodyH) / 2f
            val radius = CornerRadius(14.dp.toPx())

            val pins = 9
            val pinW = bodyW / (pins * 2f)
            val pinH = h * 0.09f
            for (i in 0 until pins) {
                val x = left + (i * 2 + 0.5f) * pinW
                drawRect(pinColor, topLeft = Offset(x, top - pinH), size = Size(pinW * 0.7f, pinH))
                drawRect(pinColor, topLeft = Offset(x, top + bodyH), size = Size(pinW * 0.7f, pinH))
            }
            val sPinH = bodyH / (pins * 2f)
            val sPinW = w * 0.035f
            for (i in 0 until pins) {
                val y = top + (i * 2 + 0.5f) * sPinH
                drawRect(pinColor, topLeft = Offset(left - sPinW, y), size = Size(sPinW, sPinH * 0.7f))
                drawRect(pinColor, topLeft = Offset(left + bodyW, y), size = Size(sPinW, sPinH * 0.7f))
            }

            drawRoundRect(bodyColor, Offset(left, top), Size(bodyW, bodyH), radius)
            drawRoundRect(
                borderColor, Offset(left, top), Size(bodyW, bodyH), radius,
                style = Stroke(width = 1.5.dp.toPx())
            )

            val padX = bodyW * 0.12f
            val padY = bodyH * 0.16f
            drawRoundRect(
                dieColor.copy(alpha = 0.20f + 0.55f * usage),
                Offset(left + padX, top + padY),
                Size(bodyW - 2 * padX, bodyH - 2 * padY),
                CornerRadius(8.dp.toPx())
            )
            drawRoundRect(
                dieColor,
                Offset(left + padX, top + padY),
                Size(bodyW - 2 * padX, bodyH - 2 * padY),
                CornerRadius(8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = borderColor,
                radius = 2.dp.toPx(),
                center = Offset(left + bodyW * 0.07f, top + bodyH * 0.09f)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (brand != CpuBrand.GENERIC && brand.displayName.isNotEmpty()) {
                Text(
                    text = brand.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = cpuName ?: stringResource(R.string.common_not_available),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            val subLine: String? = when {
                maxFreqMhz != null ->
                    stringResource(R.string.ghz_format, maxFreqMhz / 1000.0)
                coreCount > 0 ->
                    stringResource(R.string.cpu_cores) + " " + coreCount
                else -> null
            }
            if (subLine != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
