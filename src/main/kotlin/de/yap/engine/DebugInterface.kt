package de.yap.engine

import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector4f
import java.lang.management.ManagementFactory


class DebugInterface {

    companion object {
        private val log: Logger = LogManager.getLogger(DebugInterface::class.java.name)
        private const val MEMORY_BAR_HEIGHT = 0.05F
    }

    var enabled = true

    private var totalPhysicalMemoryInBytes: Long = 0
    private var freePhysicalMemoryInBytes: Long = 0
    private var usedPhysicalMemoryAtStartInBytes: Long = 0
    private var committedVirtualMemoryInBytes: Long = 0

    private var totalJvmMemoryInBytes: Long = 0
    private var freeJvmMemoryInBytes: Long = 0

    fun init() {
        if (!enabled) {
            return
        }

        updateMemoryStatistics()
        usedPhysicalMemoryAtStartInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes
    }

    fun input() {
        if (!enabled) {
            return
        }

    }

    fun update(interval: Float) {
        if (!enabled) {
            return
        }

        updateMemoryStatistics()
    }

    private fun updateMemoryStatistics() {
        totalJvmMemoryInBytes = Runtime.getRuntime().totalMemory()
        freeJvmMemoryInBytes = Runtime.getRuntime().freeMemory()

        val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        totalPhysicalMemoryInBytes = systemMXBean.totalPhysicalMemorySize
        freePhysicalMemoryInBytes = systemMXBean.freePhysicalMemorySize
        committedVirtualMemoryInBytes = systemMXBean.committedVirtualMemorySize
    }

    fun render(window: Window, renderer: Renderer, shader: Shader) {
        if (!enabled) {
            return
        }

        // TODO add descriptive text for physical memory usage
        // TODO add descriptive text for jvm memory usage

        renderer.disableDepthTest {
            shader.setUniform("projection", Matrix4f())
            shader.setUniform("view", Matrix4f().scale(1.0F / window.aspectRatio(), 1.0F, 1.0F))

            val usedJvmMemoryInBytes = totalJvmMemoryInBytes - freeJvmMemoryInBytes
            val usedPhysicalMemoryInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes

            val yOffsetPhysical = 1.0F - MEMORY_BAR_HEIGHT / 2F
            renderTotalMemory(renderer, shader, yOffsetPhysical)
            renderUsedMemory(renderer, shader, yOffsetPhysical, usedPhysicalMemoryInBytes, totalPhysicalMemoryInBytes)
            renderUsedMemory(renderer, shader, yOffsetPhysical, usedPhysicalMemoryAtStartInBytes, totalPhysicalMemoryInBytes, true)

            val yOffsetVirtual = 1.0F - MEMORY_BAR_HEIGHT * 1.5F
            renderTotalMemory(renderer, shader, yOffsetVirtual)
            renderUsedMemory(renderer, shader, yOffsetVirtual, committedVirtualMemoryInBytes, totalPhysicalMemoryInBytes)

            val yOffsetJvm = 1.0F - MEMORY_BAR_HEIGHT * 2.5F
            renderTotalMemory(renderer, shader, yOffsetJvm)
            renderUsedMemory(renderer, shader, yOffsetJvm, usedJvmMemoryInBytes, totalJvmMemoryInBytes)
        }
    }

    private fun renderTotalMemory(renderer: Renderer, shader: Shader, yOffset: Float) {
        val xScale = 1.0F
        val yScale = MEMORY_BAR_HEIGHT
        val xOffset = -0.5F
        val zOffset = -0.1F // move it a bit further to the front than the rest of the rendered stuff
        val color = Vector4f(0.0F, 1.0F, 0.0F, 1.0F)
        val transformation = Matrix4f()
                .translate(xOffset, yOffset, zOffset)
                .scale(xScale, yScale, 1.0F)
        renderer.quad(shader, transformation, color)
    }

    private fun renderUsedMemory(renderer: Renderer, shader: Shader, yOffset: Float, usedMemoryInBytes: Long, totalMemoryInBytes: Long, yellow: Boolean = false) {
        val xScale = (usedMemoryInBytes.toDouble() / totalMemoryInBytes.toDouble()).toFloat()
        val yScale = MEMORY_BAR_HEIGHT
        val xOffset = -1.0F + xScale / 2.0F
        val zOffset = -0.1F // move it a bit further to the front than the rest of the rendered stuff
        val color = if (yellow) {
            Vector4f(1.0F, 1.0F, 0.0F, 1.0F)
        } else {
            Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
        }
        val transformation = Matrix4f()
                .translate(xOffset, yOffset, zOffset)
                .scale(xScale, yScale, 1.0F)
        renderer.quad(shader, transformation, color)
    }
}
