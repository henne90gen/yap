package de.yap.engine.debug

import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW


class DebugInterface {

    companion object {
        private val log: Logger = LogManager.getLogger(DebugInterface::class.java.name)
    }

    var enabled = true

    val memory = DebugMemory()
    val cpu = DebugCPU()

    fun init(renderer: Renderer) {
        if (!enabled) {
            return
        }

        memory.init(renderer)
        cpu.init(renderer)
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

    fun render(window: Window, renderer: Renderer, shader: Shader, fontShader: Shader) {
        if (!enabled) {
            return
        }

        memory.render(window, renderer, shader, fontShader)
        cpu.render(window, renderer, shader, fontShader)
    }

    fun keyCallback(windowHandle: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW.GLFW_KEY_F1 && action == GLFW.GLFW_RELEASE) {
            enabled = !enabled
        }
    }
}
