package de.yap.engine.debug

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.engine.graphics.FontRenderer
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector4f
import java.lang.management.ManagementFactory

class DebugMemory : ISystem() {
    companion object {
        private val log: Logger = LogManager.getLogger()

        private const val MEMORY_BAR_HEIGHT = 0.05F

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

    var enabled = false

    private var totalPhysicalMemoryInBytes: Long = 0
    private var freePhysicalMemoryInBytes: Long = 0
    private var usedPhysicalMemoryAtStartInBytes: Long = 0
    private var committedVirtualMemoryInBytes: Long = 0

    private var totalJvmMemoryInBytes: Long = 0
    private var freeJvmMemoryInBytes: Long = 0

    override fun init() {
        update(0.0F, emptyList())
        usedPhysicalMemoryAtStartInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes
    }

    override fun update(interval: Float, entities: List<Entity>) {
        if (!enabled) {
            return
        }

        totalJvmMemoryInBytes = Runtime.getRuntime().totalMemory()
        freeJvmMemoryInBytes = Runtime.getRuntime().freeMemory()

        val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
        totalPhysicalMemoryInBytes = systemMXBean.totalPhysicalMemorySize
        freePhysicalMemoryInBytes = systemMXBean.freePhysicalMemorySize
        committedVirtualMemoryInBytes = systemMXBean.committedVirtualMemorySize
    }

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        val fontRenderer = YapGame.getInstance().fontRenderer
        val renderer = YapGame.getInstance().renderer

        val top = 1.035F
        val physicalTextTransformation = Matrix4f()
                .translate(-1.45F, top - MEMORY_BAR_HEIGHT, 0.0F)
                .scale(0.5F)
        fontRenderer.stringSimple("Physical Memory:", physicalTextTransformation)

        val virtualTextTransformation = Matrix4f()
                .translate(-1.42F, top - MEMORY_BAR_HEIGHT * 2.0F, 0.0F)
                .scale(0.5F)
        fontRenderer.stringSimple("Virtual Memory:", virtualTextTransformation)

        val jvmTextTransformation = Matrix4f()
                .translate(-1.345F, top - MEMORY_BAR_HEIGHT * 3.0F, 0.0F)
                .scale(0.5F)
        fontRenderer.stringSimple("JVM Memory:", jvmTextTransformation)

        val usedJvmMemoryInBytes = totalJvmMemoryInBytes - freeJvmMemoryInBytes
        val usedPhysicalMemoryInBytes = totalPhysicalMemoryInBytes - freePhysicalMemoryInBytes

        renderer.disableDepthTest {
            renderTotalMemory(fontRenderer, totalPhysicalMemoryBarTransformation)
            renderUsedMemory(fontRenderer, yOffsetPhysical, usedPhysicalMemoryInBytes, totalPhysicalMemoryInBytes)
            renderUsedMemory(fontRenderer, yOffsetPhysical, usedPhysicalMemoryAtStartInBytes, totalPhysicalMemoryInBytes, true)

            renderTotalMemory(fontRenderer, totalVirtualMemoryBarTransformation)
            renderUsedMemory(fontRenderer, yOffsetVirtual, committedVirtualMemoryInBytes, totalPhysicalMemoryInBytes)

            renderTotalMemory(fontRenderer, totalJvmMemoryBarTransformation)
            renderUsedMemory(fontRenderer, yOffsetJvm, usedJvmMemoryInBytes, totalJvmMemoryInBytes)
        }
    }

    private fun renderTotalMemory(renderer: FontRenderer, transformation: Matrix4f) {
        val color = Vector4f(0.0F, 1.0F, 0.0F, 1.0F)
        renderer.quad(transformation, color)
    }

    private fun renderUsedMemory(renderer: FontRenderer, yOffset: Float, usedMemoryInBytes: Long, totalMemoryInBytes: Long, yellow: Boolean = false) {
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
        renderer.quad(transformation, color)
    }
}
