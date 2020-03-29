package de.yap.engine.debug

import de.yap.engine.ecs.Entity
import de.yap.engine.ecs.ISystem
import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.graphics.Matrix4fUniform
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW


class DebugInterface : ISystem() {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private var enabled = true

    private val memory = DebugMemory()
    private val cpu = DebugCPU()

    override fun init() {
        memory.init()
        cpu.init()
    }

    override fun update(interval: Float, entities: List<Entity>) {
        if (!enabled) {
            return
        }

        memory.update(interval)
        cpu.update(interval)
    }

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        inScreenSpace {
            memory.render()
            cpu.render()
        }
    }

    private fun inScreenSpace(func: () -> Unit) {
        val shader = YapGame.getInstance().renderer.shader3D
        val aspectRatio = YapGame.getInstance().window.aspectRatio()
        val projection = shader.getUniform<Matrix4fUniform>("projection")!!.value
        val view = shader.getUniform<Matrix4fUniform>("view")!!.value
        shader.setUniform("projection", Matrix4f())
        shader.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))

        func()

        shader.setUniform("projection", projection)
        shader.setUniform("view", view)
    }

    @Subscribe
    fun keyCallback(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_F1 && event.action == GLFW.GLFW_RELEASE) {
            enabled = !enabled
        }
    }
}
