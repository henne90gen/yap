package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.util.MOUSE_SENSITIVITY
import de.yap.engine.util.MOVEMENT_SPEED
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW

/**
 * Controls:
 *  - W,A,S,D - move in the x-z-plane
 *  - Q,E - move along the y-axis
 *  - SPACE - switch to next camera
 */
class FirstPersonCameraSystem : ISystem(PositionComponent::class.java, RotationComponent::class.java, CameraComponent::class.java) {

    companion object {
        private val log = LogManager.getLogger()

        var currentCameraEntity: Entity? = null
    }

    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            val cameraComponent = entity.getComponent<CameraComponent>()
            if (cameraComponent.active) {
                continue
            }

            val positionComponent = entity.getComponent<PositionComponent>()
            val rotationComponent = entity.getComponent<RotationComponent>()

            // show position
            val position = positionComponent.position
            val transformation = Matrix4f()
                    .translate(position)
                    .scale(0.4F)
            YapGame.getInstance().renderer.cube(transformation, cameraComponent.color)

            // show viewing direction
            val dir = rotationComponent.direction()
            val end = Vector3f(position)
                    .add(dir)
            val viewDirColor = Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
            YapGame.getInstance().renderer.line(position, end, viewDirColor)
        }
    }

    override fun update(interval: Float, entities: List<Entity>) {
        for (entity in entities) {
            val positionComponent = entity.getComponent<PositionComponent>()
            val rotationComponent = entity.getComponent<RotationComponent>()
            val cameraComponent = entity.getComponent<CameraComponent>()

            if (!cameraComponent.active) {
                continue
            }

            currentCameraEntity = entity

            pollDirection(cameraComponent.direction)
            pollMousePosition(cameraComponent)

            val tmp = Vector3f(cameraComponent.direction).mul(MOVEMENT_SPEED)
            rotatedMove(positionComponent, rotationComponent, tmp)

            val mouseRot = Vector2f(cameraComponent.mousePosition.x, cameraComponent.mousePosition.y)
                    .mul(MOUSE_SENSITIVITY)
            rotate(rotationComponent, mouseRot)

            val pos = Vector3f(positionComponent.position)
            YapGame.getInstance().view = Matrix4f()
                    .mul(rotationComponent.rotationMatrix())
                    .translate(pos.mul(-1.0f))

            cameraComponent.mousePosition = Vector2f(0.0F)
        }
    }

    private fun pollDirection(direction: Vector3f) {
        val window = YapGame.getInstance().window
        fun keyPressed(key: Int): Boolean {
            val action = window.getKeyState(key)
            return action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT
        }

        fun keyReleased(key: Int): Boolean {
            val action = window.getKeyState(key)
            return action == GLFW.GLFW_RELEASE
        }

        direction.x = when {
            keyPressed(GLFW.GLFW_KEY_D) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_A) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_A) || keyReleased(GLFW.GLFW_KEY_D) -> {
                0.0F
            }
            else -> {
                direction.x
            }
        }
        direction.y = when {
            keyPressed(GLFW.GLFW_KEY_Q) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_E) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_Q) || keyReleased(GLFW.GLFW_KEY_E) -> {
                0.0F
            }
            else -> {
                direction.y
            }
        }
        direction.z = when {
            keyPressed(GLFW.GLFW_KEY_S) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_W) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_S) || keyReleased(GLFW.GLFW_KEY_W) -> {
                0.0F
            }
            else -> {
                direction.z
            }
        }
    }

    private fun pollMousePosition(cameraComponent: CameraComponent) {
        val mousePosition = Vector2f(0.0F)
        YapGame.getInstance().window.getMousePosition(mousePosition)
        cameraComponent.mousePosition = mousePosition
        YapGame.getInstance().window.setMousePosition(0.0, 0.0)
    }

    private fun rotatedMove(positionComponent: PositionComponent, rotationComponent: RotationComponent, offset: Vector3f) {
        val rotatedOffset = Vector4f(offset.x, offset.y, offset.z, 0.0F)
                .mul(rotationComponent.rotationMatrix().invert())

        positionComponent.position.add(Vector3f(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z))
    }

    fun rotate(rotationComponent: RotationComponent, rotation: Vector2f) {
        rotationComponent.pitch -= rotation.y
        rotationComponent.yaw += rotation.x
    }

    @Subscribe
    fun onKeyboard(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_SPACE && event.action == GLFW.GLFW_RELEASE) {
            switchActiveCamera()
        }
    }

    private fun switchActiveCamera() {
        val entities = YapGame.getInstance().entityManager.getEntities(capability)
        var activeIndex = -1
        for (entityWithIndex in entities.withIndex()) {
            val entity = entityWithIndex.value
            val cameraComponent = entity.getComponent<CameraComponent>()

            if (!cameraComponent.active) {
                continue
            }

            activeIndex = entityWithIndex.index
            cameraComponent.active = false
            break
        }

        activeIndex++
        activeIndex %= entities.size

        val newActiveEntity = entities[activeIndex]
        val cameraComponent = newActiveEntity.getComponent<CameraComponent>()
        cameraComponent.active = true
    }
}
