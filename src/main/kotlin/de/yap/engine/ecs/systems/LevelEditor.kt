package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.game.IntersectionResult
import de.yap.game.TransformedMesh
import de.yap.game.YapGame
import de.yap.game.intersects
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW


class LevelEditor : ISystem(MeshComponent::class.java, PositionComponent::class.java) {

    private var clampedPoint: Vector3f? = null
    private var normal: Vector3f? = null

    override fun init() {
        // TODO open up a window to show current block settings and load/save buttons
//        val frame = JFrame() // creating instance of JFrame
//
//        val button = JButton("click") // creating instance of JButton
//        button.setBounds(130, 100, 100, 40) // x axis, y axis, width, height
//        frame.add(button) // adding button in JFrame
//
//        frame.setSize(400, 500) // 400 width and 500 height
//        frame.layout = null // using no layout managers
//        frame.isVisible = true // making the frame visible
    }

    @Subscribe
    fun onMouseClick(event: MouseClickEvent) {
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_1 && event.action == GLFW.GLFW_RELEASE) {
            removeBlock()
        }
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_2 && event.action == GLFW.GLFW_RELEASE) {
            placeBlock()
        }
    }

    @Subscribe
    fun onKeyboardPress(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_LEFT_ALT && event.action == GLFW.GLFW_RELEASE) {
            releaseMouse()
        }
    }

    private fun releaseMouse() {
        YapGame.getInstance().window.toggleMouseVisibility()
        YapGame.getInstance().firstPersonCamera.toggleMouseMovementTracking()
    }

    private fun removeBlock() {
        clampedPoint?.let { p ->
            normal?.let { n ->
                val removalPoint = Vector3f(p).sub(n)
                // TODO use a spacial query
                val entities = YapGame.getInstance().entityManager.getEntities(capability)
                for (entity in entities) {
                    val position = entity.getComponent<PositionComponent>().position
                    if (removalPoint == position) {
                        YapGame.getInstance().entityManager.removeEntity(entity)
                        break
                    }
                }
            }
        }
    }

    private fun placeBlock() {
        clampedPoint?.let {
            val entity = BlockEntity(it)
            YapGame.getInstance().entityManager.addEntity(entity)
        }
    }

    override fun render(entities: List<Entity>) {
        renderCrosshair()
        renderSelectedBlock()
    }

    private fun renderSelectedBlock() {
        clampedPoint?.let {
            val renderer = YapGame.getInstance().renderer
            renderer.wireframe {
                val transformation = Matrix4f()
                        .translate(it)
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.cube(transformation, color)
            }
        }
    }

    private fun renderCrosshair() {
        val renderer = YapGame.getInstance().renderer
        renderer.inScreenSpace {
            val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)

            val transformationHorizontal = Matrix4f()
                    .scale(0.1F, 0.005F, 1.0F)
            renderer.quad(transformationHorizontal, color)

            val transformationVertical = Matrix4f()
                    .scale(0.005F, 0.1F, 1.0F)
            renderer.quad(transformationVertical, color)
        }
    }

    override fun update(interval: Float, entities: List<Entity>) {
        val meshes = entities.map {
            TransformedMesh(
                    it.getComponent<MeshComponent>().mesh,
                    Matrix4f().translate(it.getComponent<PositionComponent>().position)
            )
        }
        val cameraEntity = YapGame.getInstance().firstPersonCamera.getCurrentCamera()
        var rayStart = Vector3f(0.0F)
        var direction = Vector3f(0.0F, 0.0F, -1.0F)
        cameraEntity?.let {
            rayStart = it.getComponent<PositionComponent>().position
            direction = it.getComponent<RotationComponent>().direction()
        }
        val intersectionResult = intersects(rayStart, direction, meshes)
        if (intersectionResult.hasValue()) {
            clampedPoint = clampPoint(intersectionResult)
            normal = intersectionResult.normal
        } else {
            clampedPoint = null
            normal = null
        }
    }

    private fun clampPoint(intersectionResult: IntersectionResult): Vector3f? {
        if (!intersectionResult.hasValue()) {
            return null
        }

        val normal = intersectionResult.normal
        val point = Vector3f(intersectionResult.point).add(Vector3f(normal).absolute().mul(0.01F))

        var x = point.x
        x = clamp(x)
        x += if (normal.x == 0.0F) {
            0.5F
        } else {
            0.5F * normal.x
        }

        var y = point.y
        y = clamp(y)
        y += if (normal.y == 0.0F) {
            0.5F
        } else {
            0.5F * normal.y
        }

        var z = point.z
        z = clamp(z)
        z += if (normal.z == 0.0F) {
            0.5F
        } else {
            0.5F * normal.z
        }

        return Vector3f(x, y, z)
    }

    private fun clamp(num: Float): Float {
        return if (num < 0.0F) {
            (num - 1.0F).toInt().toFloat()
        } else {
            num.toInt().toFloat()
        }
    }
}
