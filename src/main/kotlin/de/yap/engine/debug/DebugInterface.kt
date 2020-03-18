package de.yap.engine.debug

import de.yap.engine.Window
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW


class DebugInterface {

    companion object {
        private val log: Logger = LogManager.getLogger(DebugInterface::class.java.name)
    }

    var enabled = true

    val memory = DebugMemory()
    val cpu = DebugCPU()

    fun init(fontRenderer: FontRenderer) {
        if (!enabled) {
            return
        }

        memory.init(fontRenderer)
        cpu.init(fontRenderer)
    }

    fun input() {
        if (!enabled) {
            return
        }

        memory.input()
        cpu.input()
    }

    fun update(interval: Float) {
        if (!enabled) {
            return
        }

        memory.update(interval)
        cpu.update(interval)
    }

    fun render(window: Window) {
        if (!enabled) {
            return
        }
        val shader = YapGame.getInstance().renderer.shader3D
        shader.setUniform("projection", Matrix4f())
        shader.setUniform("view", Matrix4f().scale(1.0F / window.aspectRatio(), 1.0F, 1.0F))

        memory.render()
        cpu.render()
    }

    fun keyCallback(windowHandle: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW.GLFW_KEY_F1 && action == GLFW.GLFW_RELEASE) {
            enabled = !enabled
        }
    }
}
