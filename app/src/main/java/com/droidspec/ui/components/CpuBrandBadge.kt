package com.droidspec.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.droidspec.R
import com.droidspec.domain.policy.CpuBrand

/**
 * Processor brand artwork. Uses the vendor chip photos shipped in
 * drawable-nodpi, displayed unmodified (original colors, composition and
 * backgrounds preserved). Brand artwork is a trademark of its owner and is
 * shown for identification only. GENERIC falls back to the app's chip icon.
 */
@Composable
fun CpuBrandBadge(
    brand: CpuBrand,
    modifier: Modifier = Modifier
) {
    val artwork = when (brand) {
        CpuBrand.SNAPDRAGON -> R.drawable.chip_snapdragon
        CpuBrand.MEDIATEK -> R.drawable.chip_mediatek
        CpuBrand.EXYNOS -> R.drawable.chip_exynos
        CpuBrand.KIRIN -> R.drawable.chip_kirin
        CpuBrand.TENSOR -> R.drawable.chip_tensor
        CpuBrand.UNISOC -> R.drawable.chip_unisoc
        CpuBrand.APPLE_A -> R.drawable.chip_apple_a
        CpuBrand.APPLE_M -> R.drawable.chip_apple_m
        CpuBrand.GENERIC -> R.drawable.ic_launcher
    }

    Column(
        modifier = modifier.wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(artwork),
            contentDescription = brand.displayName.ifEmpty { "CPU" },
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(210.dp)
                .clip(RoundedCornerShape(18.dp))
        )
        if (brand.displayName.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = brand.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
