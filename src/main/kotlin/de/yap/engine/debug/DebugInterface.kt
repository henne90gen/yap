package de.yap.engine.debug

import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.Subscribe
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

    // TODO use EntityManager
//    @Subscribe
//    fun init(event: InitEvent) {
//        if (!enabled) {
//            return
//        }
//
//        memory.init()
//        cpu.init()
//    }

    // TODO use EntityManager
//    @Subscribe
//    fun input(event: InputEvent) {
//        if (!enabled) {
//            return
//        }
//
//        memory.input()
//        cpu.input()
//    }

    // TODO use EntityManager
//    @Subscribe
//    fun update(event: UpdateEvent) {
//        if (!enabled) {
//            return
//        }
//
//        memory.update(event.interval)
//        cpu.update(event.interval)
//    }

    // TODO use EntityManager
//    @Subscribe
//    fun render(event: RenderEvent) {
//        if (!enabled) {
//            return
//        }
//        val shader = YapGame.getInstance().renderer.shader3D
//        shader.setUniform("projection", Matrix4f())
//        shader.setUniform("view", Matrix4f().scale(1.0F / YapGame.getInstance().window.aspectRatio(), 1.0F, 1.0F))
//
//        memory.render()
//        cpu.render()
//    }

    @Subscribe
    fun keyCallback(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_F1 && event.action == GLFW.GLFW_RELEASE) {
            enabled = !enabled
        }
    }
}
