package de.yap.engine.debug

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.engine.graphics.Text
import de.yap.game.YapGame
import org.joml.Matrix4f
import java.lang.management.ManagementFactory
import java.text.DecimalFormat


class DebugCPU : ISystem() {

    var enabled = false

    private val df = DecimalFormat("#.00")

    private var processLoad = 0.0

    private var updateCounter = 0

    override fun update(interval: Float, entities: List<Entity>) {
        if (!enabled) {
            return
        }
        updateCounter++
        if (updateCounter % 4 != 0) {
            return
        }

        val systemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

        processLoad = systemMXBean.processCpuLoad * 100.0
    }

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        val fontRenderer = YapGame.getInstance().fontRenderer
        val transform = Matrix4f()
                .translate(0.25F, 0.90F, 0.0F)
                .scale(0.3F)
        fontRenderer.stringSimple("Process: ${df.format(processLoad)}%", transform)
    }
}
