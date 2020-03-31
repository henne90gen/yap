package de.yap.engine.debug

import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.FirstPersonCameraSystem
import de.yap.engine.ecs.systems.ISystem
import de.yap.engine.graphics.Matrix4fUniform
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW


class DebugInterface : ISystem() {

    private var enabled = false

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

        YapGame.getInstance().renderer.inScreenSpace {
            memory.render()
            cpu.render()
        }

        renderCoordinateSystem()
    }

    private fun renderCoordinateSystem() {
        val renderer = YapGame.getInstance().renderer
        val currentCamera = FirstPersonCameraSystem.currentCameraEntity
        currentCamera?.let {
            val positionComponent = currentCamera.getComponent<PositionComponent>()
            val rotationComponent = currentCamera.getComponent<RotationComponent>()

            val position = positionComponent.position
            val direction = rotationComponent.direction()
            val transformation = Matrix4f()
                    .translate(position)
                    .translate(direction)
                    .scale(0.075F)
            val origin = Vector3f(0.0F, 0.0F, 0.0F)
            val xEnd = Vector3f(1.0F, 0.0F, 0.0F)
            val yEnd = Vector3f(0.0F, 1.0F, 0.0F)
            val zEnd = Vector3f(0.0F, 0.0F, 1.0F)

            transformation.transformPosition(origin)
            transformation.transformPosition(xEnd)
            transformation.transformPosition(yEnd)
            transformation.transformPosition(zEnd)

            renderer.line(origin, xEnd, Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
            renderer.line(origin, yEnd, Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
            renderer.line(origin, zEnd, Vector4f(0.0F, 0.0F, 1.0F, 1.0F))
        }
    }

    @Subscribe
    fun keyCallback(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_F1 && event.action == GLFW.GLFW_RELEASE) {
            enabled = !enabled
        }
    }
}
