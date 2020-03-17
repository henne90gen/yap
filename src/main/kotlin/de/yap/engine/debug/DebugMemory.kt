package de.yap.engine.debug

import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector4f
import java.lang.management.ManagementFactory

class DebugMemory {
    companion object {
        private val log: Logger = LogManager.getLogger(DebugMemory::class.java.name)

        private const val MEMORY_BAR_HEIGHT = 0.05F
        private val physicalTextTransformation = Matrix4f()
                .translate(-1.45F, 1.0F - MEMORY_BAR_HEIGHT, 0.0F)
                .scale(0.3F)
        private val virtualTextTransformation = Matrix4f()
                .translate(-1.42F, 1.0F - MEMORY_BAR_HEIGHT * 2.0F, 0.0F)
                .scale(0.3F)
        private val jvmTextTransformation = Matrix4f()
                .translate(-1.315F, 1.0F - MEMORY_BAR_HEIGHT * 3.0F, 0.0F)
                .scale(0.3F)

        private const val yOffsetPhysical = 1.0F - MEMORY_BAR_HEIGHT / 2F
        private const val yOffsetVirtual = 1.0F - MEMORY_BAR_HEIGHT * 1.5F
        private const val yOffsetJvm = 1.0F - MEMORY_BAR_HEIGHT * 2.5F

        private val totalPhysicalMemoryBarTransformation = calculateTotalMemoryBarTransformation(yOffsetPhysical)
        private val totalVirtualMemoryBarTransformation = calculateTotalMemoryBarTransformation(yOffsetVirtual)
        private val totalJvmMemoryBarTransformation = calculateTotalMemoryBarTransformation(yOffsetJvm)

        private fun calculateTotalMemoryBarTransformation(yOffset: Float): Matrix4f {
            val xScale = 1.0F
            val yScale = MEMORY_BAR_HEIGHT
            val xOffset = -0.5F
            val zOffset = -0.1F // move it a bit further to the front than the rest of the rendered stuff
            return Matrix4f()
                    .translate(xOffset, yOffset, zOffset)
                    .scale(xScale, yScale, 1.0F)
        }
    }

    private var totalPhysicalMemoryInBytes: Long = 0
    private var freePhysicalMemoryInBytes: Long = 0
    private var usedPhysicalMemoryAtStartInBytes: Long = 0
    private var committedVirtualMemoryInBytes: Long = 0

    private var totalJvmMemoryInBytes: Long = 0
    private var freeJvmMemoryInBytes: Long = 0

    private var physicalMemoryMesh: Mesh? = null
    private var virtualMemoryMesh: Mesh? = null
    private var jvmMemoryMesh: Mesh? = null

    fun init(renderer: Renderer) {
        update(0.0F)

        usedPhysicalMemoryAtStartInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes

        physicalMemoryMesh = MeshUtils.text(renderer.font, "Physical Memory:")
        virtualMemoryMesh = MeshUtils.text(renderer.font, "Virtual Memory:")
        jvmMemoryMesh = MeshUtils.text(renderer.font, "JVM Memory:")
    }

    fun input() {
    }

    fun update(interval: Float) {
        totalJvmMemoryInBytes = Runtime.getRuntime().totalMemory()
        freeJvmMemoryInBytes = Runtime.getRuntime().freeMemory()

        val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        totalPhysicalMemoryInBytes = systemMXBean.totalPhysicalMemorySize
        freePhysicalMemoryInBytes = systemMXBean.freePhysicalMemorySize
        committedVirtualMemoryInBytes = systemMXBean.committedVirtualMemorySize
    }

    fun render(window: Window, renderer: Renderer, shader: Shader, fontShader: Shader) {
        renderer.disableDepthTest {
            renderer.uiText(fontShader, window.aspectRatio(), physicalMemoryMesh!!, physicalTextTransformation)
            renderer.uiText(fontShader, window.aspectRatio(), virtualMemoryMesh!!, virtualTextTransformation)
            renderer.uiText(fontShader, window.aspectRatio(), jvmMemoryMesh!!, jvmTextTransformation)

            shader.setUniform("projection", Matrix4f())
            shader.setUniform("view", Matrix4f().scale(1.0F / window.aspectRatio(), 1.0F, 1.0F))

            val usedJvmMemoryInBytes = totalJvmMemoryInBytes - freeJvmMemoryInBytes
            val usedPhysicalMemoryInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes

            renderTotalMemory(renderer, shader, totalPhysicalMemoryBarTransformation)
            renderUsedMemory(renderer, shader, yOffsetPhysical, usedPhysicalMemoryInBytes, totalPhysicalMemoryInBytes)
            renderUsedMemory(renderer, shader, yOffsetPhysical, usedPhysicalMemoryAtStartInBytes, totalPhysicalMemoryInBytes, true)

            renderTotalMemory(renderer, shader, totalVirtualMemoryBarTransformation)
            renderUsedMemory(renderer, shader, yOffsetVirtual, committedVirtualMemoryInBytes, totalPhysicalMemoryInBytes)

            renderTotalMemory(renderer, shader, totalJvmMemoryBarTransformation)
            renderUsedMemory(renderer, shader, yOffsetJvm, usedJvmMemoryInBytes, totalJvmMemoryInBytes)
        }
    }

    private fun renderTotalMemory(renderer: Renderer, shader: Shader, transformation: Matrix4f) {
        val color = Vector4f(0.0F, 1.0F, 0.0F, 1.0F)
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