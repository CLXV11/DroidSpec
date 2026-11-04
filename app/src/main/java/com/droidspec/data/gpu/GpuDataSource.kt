package com.droidspec.data.gpu

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLES30
import com.droidspec.domain.model.GpuInfo

/**
 * Reads GLES renderer/vendor/extensions through a temporary, surfaceless
 * EGL context, plus Vulkan feature flags from PackageManager.
 */
class GpuDataSource(private val context: Context) {

    fun snapshot(): GpuInfo {
        val gl = readGlStrings()
        return GpuInfo(
            renderer = gl.renderer,
            vendor = gl.vendor,
            glesVersion = gl.version,
            vulkan = readVulkan(),
            extensions = gl.extensions
        )
    }

    private class GlStrings(
        val renderer: String?,
        val vendor: String?,
        val version: String?,
        val extensions: List<String>
    )

    private fun readGlStrings(): GlStrings {
        val noGl = GlStrings(null, null, null, emptyList())
        val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return noGl
        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) return noGl

        val configAttrs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(display, configAttrs, 0, configs, 0, 1, numConfigs, 0)) {
            EGL14.eglTerminate(display)
            return noGl
        }
        val surfaceAttrs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        val surface: EGLSurface? =
            EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttrs, 0)
        val contextAttrs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        val eglContext: EGLContext? =
            EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttrs, 0)

        var renderer: String? = null
        var vendor: String? = null
        var glVersion: String? = null
        var extensions: List<String> = emptyList()

        if (surface != null && eglContext != null &&
            EGL14.eglMakeCurrent(display, surface, surface, eglContext)
        ) {
            renderer = GLES20.glGetString(GLES20.GL_RENDERER)?.ifBlank { null }
            vendor = GLES20.glGetString(GLES20.GL_VENDOR)?.ifBlank { null }
            glVersion = GLES20.glGetString(GLES20.GL_VERSION)?.ifBlank { null }
            extensions = readExtensions()
            EGL14.eglMakeCurrent(
                display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
            )
        }

        eglContext?.let { EGL14.eglDestroyContext(display, it) }
        surface?.let { EGL14.eglDestroySurface(display, it) }
        EGL14.eglTerminate(display)
        return GlStrings(renderer, vendor, glVersion, extensions)
    }

    private fun readExtensions(): List<String> = runCatching {
        val count = IntArray(1)
        GLES20.glGetIntegerv(GLES30.GL_NUM_EXTENSIONS, count, 0)
        if (count[0] <= 0) return emptyList()
        (0 until count[0]).mapNotNull { GLES30.glGetStringi(GLES30.GL_EXTENSIONS, it) }
            .filter { it.isNotBlank() }
    }.getOrElse {
        GLES20.glGetString(GLES20.GL_EXTENSIONS)?.split(' ')?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    private fun readVulkan(): String? = runCatching {
        val pm = context.packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)) {
            null
        } else {
            val feature = pm.systemAvailableFeatures
                ?.firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }
            val v = feature?.version ?: 0
            if (v <= 0) "Supported"
            else "${v shr 22}.${(v shr 12) and 0x3FF}.${v and 0xFFF}"
        }
    }.getOrNull()
}
