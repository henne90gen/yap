package de.yap.engine.debug

import de.yap.engine.graphics.Text
import de.yap.game.YapGame
import org.joml.Matrix4f
import java.lang.management.ManagementFactory
import java.text.DecimalFormat


class DebugCPU {

    private val df = DecimalFormat("#.00")

    private var systemText: Text? = null
    private var systemLoadText: Text? = null

    private var processText: Text? = null
    private var processLoadText: Text? = null

    private var updateCounter = 0

    fun init() {
        val fontRenderer = YapGame.getInstance().fontRenderer
        val systemTextTransform = Matrix4f()
                .translate(0.25F, 0.95F, 0.0F)
                .scale(0.3F)
        systemText = Text("System:", fontRenderer.font, systemTextTransform)

        val systemLoadTransform = Matrix4f()
                .translate(0.5F, 0.95F, 0.0F)
                .scale(0.3F)
        systemLoadText = Text("", fontRenderer.font, systemLoadTransform)

        val processTextTransform = Matrix4f()
                .translate(0.25F, 0.90F, 0.0F)
                .scale(0.3F)
        processText = Text("Process:", fontRenderer.font, processTextTransform)

        val processLoadTransform = Matrix4f()
                .translate(0.5F, 0.90F, 0.0F)
                .scale(0.3F)
        processLoadText = Text("", fontRenderer.font, processLoadTransform)
    }

    fun input() {
    }

    fun update(interval: Float) {
        updateCounter++
        if (updateCounter % 4 != 0) {
            return
        }

        val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

        val systemLoad = systemMXBean.systemCpuLoad * 100.0
        systemLoadText?.updateString("${df.format(systemLoad)}%")

        val processLoad = systemMXBean.processCpuLoad * 100.0
        processLoadText?.updateString("${df.format(processLoad)}%")
    }

    fun render() {
        val fontRenderer = YapGame.getInstance().fontRenderer
        systemText?.let { fontRenderer.string(it) }
        systemLoadText?.let { fontRenderer.string(it) }

        processText?.let { fontRenderer.string(it) }
        processLoadText?.let { fontRenderer.string(it) }
    }
}
