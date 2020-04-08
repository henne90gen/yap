package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.util.MOUSE_SENSITIVITY
import de.yap.engine.util.MOVEMENT_SPEED
import de.yap.engine.util.Y_AXIS
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
 *  - TAB - switch to next camera
 */
class CameraSystem : ISystem(PositionComponent::class.java, RotationComponent::class.java, CameraComponent::class.java) {

    companion object {
        private val log = LogManager.getLogger()
    }

    private var currentCameraEntity: Entity? = null
    private var trackMouseMovement = false

    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            renderEntity(entity)
        }
    }

    private fun renderEntity(entity: Entity) {
        val cameraComponent = entity.getComponent<CameraComponent>()
        if (cameraComponent.active) {
            return
        }

        val positionComponent = entity.getComponent<PositionComponent>()
        val rotationComponent = entity.getComponent<RotationComponent>()

        // show position
        val position = positionComponent.position
        val transformation = Matrix4f()
                .translate(position)
                .translate(cameraComponent.offset)
                .scale(0.4F)
        YapGame.getInstance().renderer.cube(transformation, cameraComponent.color)

        // show viewing direction
        val dir = rotationComponent.direction()
        val end = Vector3f(position)
                .add(dir)
        val viewDirColor = Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
        YapGame.getInstance().renderer.line(position, end, viewDirColor)
    }

    override fun update(interval: Float, entities: List<Entity>) {
        if (!trackMouseMovement) {
            return
        }

        for (entity in entities) {
            updateEntity(entity)
        }
    }

    private fun updateEntity(entity: Entity) {
        val cameraComponent = entity.getComponent<CameraComponent>()
        if (!cameraComponent.active) {
            return
        }

        currentCameraEntity = entity

        when (cameraComponent.type) {
            CameraType.FIRST_PERSON -> updateFirstPersonCamera(entity, cameraComponent)
            CameraType.THIRD_PERSON -> updateThirdPersonCamera(entity, cameraComponent)
        }
    }

    private fun updateFirstPersonCamera(entity: Entity, cameraComponent: CameraComponent) {
        val positionComponent = entity.getComponent<PositionComponent>()
        val rotationComponent = entity.getComponent<RotationComponent>()

        pollDirection(cameraComponent.direction)
        pollMousePosition(cameraComponent)

        val offset = Vector3f(cameraComponent.direction).mul(MOVEMENT_SPEED)
        val rotatedOffset = Vector4f(offset.x, offset.y, offset.z, 0.0F)
                .mul(Matrix4f().rotate(rotationComponent.yaw, Y_AXIS).invert())

        positionComponent.position.add(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z)

        val mouseRot = Vector2f(cameraComponent.mousePosition.x, cameraComponent.mousePosition.y)
                .mul(MOUSE_SENSITIVITY)
        rotate(rotationComponent, mouseRot)

        val pos = Vector3f(positionComponent.position)
        YapGame.getInstance().view = Matrix4f()
                .mul(rotationComponent.rotationMatrix())
                .translate(pos.mul(-1.0f))

        cameraComponent.mousePosition = Vector2f(0.0F)
    }

    private fun updateThirdPersonCamera(entity: Entity, cameraComponent: CameraComponent) {
        val positionComponent = entity.getComponent<PositionComponent>()
        val rotationComponent = entity.getComponent<RotationComponent>()

        pollDirection(cameraComponent.direction)
        pollMousePosition(cameraComponent)

        val offset = Vector3f(cameraComponent.direction).mul(MOVEMENT_SPEED)
        val rotatedOffset = Vector4f(offset.x, offset.y, offset.z, 0.0F)
                .mul(Matrix4f().rotate(rotationComponent.yaw, Y_AXIS).invert())

        positionComponent.position.add(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z)

//        val mouseRot = Vector2f(cameraComponent.mousePosition.x, cameraComponent.mousePosition.y)
//                .mul(MOUSE_SENSITIVITY)
//        rotate(rotationComponent, mouseRot)
        if (offset != Vector3f(0.0F)) {
            cameraComponent.offset = Vector3f(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z).normalize().mul(-1.0F)
            cameraComponent.offset.y += 1.0F
            cameraComponent.offset.mul(2.0F)
        }

        val pos = Vector3f(positionComponent.position).add(cameraComponent.offset)
        YapGame.getInstance().view = Matrix4f()
//                .mul(rotationComponent.rotationMatrix())
//                .translate(pos.mul(-1.0f))
                .lookAt(pos, positionComponent.position, Vector3f(0.0F, 1.0F, 0.0F))

        cameraComponent.mousePosition = Vector2f(0.0F)
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
            keyPressed(GLFW.GLFW_KEY_SPACE) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_SPACE) || keyReleased(GLFW.GLFW_KEY_LEFT_SHIFT) -> {
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

    fun rotate(rotationComponent: RotationComponent, rotation: Vector2f) {
        rotationComponent.pitch -= rotation.y
        rotationComponent.yaw += rotation.x
    }

    @Subscribe
    fun onKeyboard(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_TAB && event.action == GLFW.GLFW_RELEASE) {
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

    fun toggleMouseMovementTracking() {
        trackMouseMovement = !trackMouseMovement
        YapGame.getInstance().window.setMousePosition(0.0, 0.0)
    }

    fun getCurrentCamera(): Entity? {
        return currentCameraEntity
    }
}
