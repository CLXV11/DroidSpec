package com.droidspec.domain.benchmark

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * DroidSpec Internal GPU benchmark — the hard one.
 *
 * Renders a rotating grid of cubes with HAND-WRITTEN GLES20 shaders on an
 * offscreen EGL pbuffer surface, presenting real frames via eglSwapBuffers,
 * and counts how many frames complete in [seconds]. This is a real GPU
 * workload (vertex transformation in-shader + 2,400 triangles per frame) —
 * and it is NOT comparable to 3DMark, Geekbench or Antutu.
 */
object GpuBench {
    const val CUBES = 200
    private const val VERTS_PER_CUBE = 36
    private const val TRIS_PER_CUBE = 12

    data class Result(val fps: Float, val trianglesPerFrame: Int)

    fun run(seconds: Int = 2): Result? {
        val totalTris = CUBES * TRIS_PER_CUBE

        val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return null
        val ver = IntArray(2)
        if (!EGL14.eglInitialize(display, ver, 0, ver, 1)) return null
        try {
            val cfgAttr = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val num = IntArray(1)
            if (!EGL14.eglChooseConfig(display, cfgAttr, 0, configs, 0, 1, num, 0)) return null

            val surfAttr = intArrayOf(EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE)
            val surface: EGLSurface? =
                EGL14.eglCreatePbufferSurface(display, configs[0], surfAttr, 0)
            val ctxAttr = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val ctx: EGLContext? =
                EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0)
            if (surface == null || ctx == null) return null
            if (!EGL14.eglMakeCurrent(display, surface, surface, ctx)) return null
            try {
                val program = buildProgram() ?: return null
                GLES20.glUseProgram(program)

                // One big static vertex buffer: the whole cube grid.
                val data = FloatArray(CUBES * VERTS_PER_CUBE * 3)
                var p = 0
                for (cx in 0 until 20) {
                    for (cy in 0 until 10) {
                        p = putCube(data, p, (cx - 10) * 0.3f, (cy - 5) * 0.3f)
                    }
                }
                val buf: java.nio.FloatBuffer =
                    ByteBuffer.allocateDirect(data.size * 4)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer()
                buf.put(data).position(0)

                val posLoc = GLES20.glGetAttribLocation(program, "aPos")
                GLES20.glEnableVertexAttribArray(posLoc)
                GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, buf)
                val mvpLoc = GLES20.glGetUniformLocation(program, "uMvp")
                val angLoc = GLES20.glGetUniformLocation(program, "uAng")
                val colLoc = GLES20.glGetUniformLocation(program, "uColor")
                GLES20.glUniform4f(colLoc, 0.10f, 0.85f, 0.70f, 1f)

                GLES20.glViewport(0, 0, 64, 64)
                val mvp = FloatArray(16)
                Matrix.setIdentityM(mvp, 0)
                Matrix.orthoM(mvp, 0, -3.2f, 3.2f, -3.2f, 3.2f, -10f, 10f)
                GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)
                GLES20.glClearColor(0f, 0f, 0f, 1f)

                var frames = 0
                val start = System.nanoTime()
                while (System.nanoTime() - start < seconds * 1_000_000_000L) {
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                    GLES20.glUniform1f(angLoc, frames * 0.045f)
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, CUBES * VERTS_PER_CUBE)
                    EGL14.eglSwapBuffers(display, surface)
                    frames++
                }
                val fps = frames * 1_000_000_000f / (System.nanoTime() - start)
                return Result(fps, totalTris)
            } finally {
                EGL14.eglMakeCurrent(
                    display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
                )
                ctx?.let { EGL14.eglDestroyContext(display, it) }
                surface?.let { EGL14.eglDestroySurface(display, it) }
            }
        } finally {
            EGL14.eglTerminate(display)
        }
    }

    /** Appends 36 vertices (12 triangles) of a cube at grid offset (ox, oy). */
    private fun putCube(d: FloatArray, p0: Int, ox: Float, oy: Float): Int {
        val s = 0.11f
        val z = 0f
        val v = floatArrayOf(
            ox - s, oy - s, z + s, ox + s, oy - s, z + s, ox + s, oy + s, z + s,
            ox - s, oy - s, z + s, ox + s, oy + s, z + s, ox - s, oy + s, z + s,
            ox + s, oy - s, z - s, ox - s, oy - s, z - s, ox - s, oy + s, z - s,
            ox + s, oy - s, z - s, ox - s, oy + s, z - s, ox + s, oy + s, z - s,
            ox - s, oy - s, z - s, ox - s, oy - s, z + s, ox - s, oy + s, z + s,
            ox - s, oy - s, z - s, ox - s, oy + s, z + s, ox - s, oy + s, z - s,
            ox + s, oy - s, z + s, ox + s, oy - s, z - s, ox + s, oy + s, z - s,
            ox + s, oy - s, z + s, ox + s, oy + s, z - s, ox + s, oy + s, z + s,
            ox - s, oy + s, z + s, ox + s, oy + s, z + s, ox + s, oy + s, z - s,
            ox - s, oy + s, z + s, ox + s, oy + s, z - s, ox - s, oy + s, z - s,
            ox - s, oy - s, z - s, ox + s, oy - s, z - s, ox + s, oy - s, z + s,
            ox - s, oy - s, z - s, ox + s, oy - s, z + s, ox - s, oy - s, z + s
        )
        System.arraycopy(v, 0, d, p0, v.size)
        return p0 + v.size
    }

    private fun buildProgram(): Int? {
        val vs = compileShader(GLES20.GL_VERTEX_SHADER, VS) ?: return null
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, FS) ?: return null
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        val ok = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, ok, 0)
        return if (ok[0] != 0) prog else null
    }

    private fun compileShader(type: Int, src: String): Int? {
        val sh = GLES20.glCreateShader(type)
        GLES20.glShaderSource(sh, src)
        GLES20.glCompileShader(sh)
        val ok = IntArray(1)
        GLES20.glGetShaderiv(sh, GLES20.GL_COMPILE_STATUS, ok, 0)
        return if (ok[0] != 0) sh else null
    }

    // Rotation is done IN THE SHADER per-vertex (phase offset by x position).
    private const val VS =
        "attribute vec4 aPos; uniform mat4 uMvp; uniform float uAng; " +
            "void main(){ float a = uAng + aPos.x * 0.35; " +
            "float c = cos(a); float s = sin(a); " +
            "vec4 p = vec4(aPos.x * c - aPos.z * s, aPos.y, aPos.x * s + aPos.z * c, 1.0); " +
            "gl_Position = uMvp * p; }"
    private const val FS =
        "precision mediump float; uniform vec4 uColor; " +
            "void main(){ gl_FragColor = uColor; }"
}
