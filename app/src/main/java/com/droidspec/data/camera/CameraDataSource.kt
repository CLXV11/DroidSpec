package com.droidspec.data.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import android.view.SurfaceHolder
import com.droidspec.core.util.CalcUtils
import com.droidspec.domain.model.CameraDetail
import com.droidspec.domain.model.CameraSummary

/**
 * Camera metadata via Camera2 characteristics only. No camera permission is
 * required to read characteristics, and no pictures are ever taken.
 */
class CameraDataSource(context: Context) {
    private val app = context.applicationContext

    fun snapshot(): CameraSummary = runCatching {
        val manager = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val details = manager.cameraIdList.map { id ->
            val c = manager.getCameraCharacteristics(id)
            val facing = c.get(CameraCharacteristics.LENS_FACING)
            val array = c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
            val physical = c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
            val afModes = c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            val outputs = runCatching {
                c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(SurfaceHolder::class.java)
                    ?.sortedByDescending { it.width * it.height }
                    ?.take(12)
                    ?.map { formatSize(it) }
            }.getOrNull() ?: emptyList()

            CameraDetail(
                id = id,
                facingKey = when (facing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "front"
                    CameraCharacteristics.LENS_FACING_BACK -> "back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "external"
                    else -> "unknown"
                },
                megapixelsCalculated = array?.let { CalcUtils.megapixels(it.width, it.height) },
                sensorSizeMm = physical?.let { "%.2f x %.2f mm".format(it.width, it.height) },
                focalLengthsMm = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.toList() ?: emptyList(),
                apertures = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    ?.toList() ?: emptyList(),
                autofocusSupported = afModes?.any {
                    it != CameraCharacteristics.CONTROL_AF_MODE_OFF
                },
                flashSupported = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE),
                outputSizes = outputs
            )
        }
        CameraSummary(totalCount = details.size, cameras = details)
    }.getOrElse { CameraSummary(0, emptyList()) }

    private fun formatSize(size: Size): String = "${size.width} x ${size.height}"
}
