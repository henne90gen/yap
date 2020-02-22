package de.yap.game

import de.yap.engine.Camera
import de.yap.engine.IGameLogic
import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.engine.mesh.Mesh
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {

    companion object {
        private val log: Logger = LogManager.getLogger(YapGame::class.java.name)
    }

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")
    private val camera = Camera(Vector3f(0.5F, 0.0F, 3.0F))
    private val cameraSpeed = 0.1F
    private var cameraRayStart = Vector3f()
    private var cameraRayResult = IntersectionResult()
    private var roomWireframe = false

    private var mousePosition = Vector2f()
    private val mouseSensitivity = 0.5F

    private var roomMeshes: List<Mesh> = emptyList()
    private val scale = 2.0F
    private val negativeScaleHalf = -0.5F * scale
    private val position = Vector3f(negativeScaleHalf)
    private val roomTransformation = Matrix4f().translate(position).scale(scale)

    private var cubeMesh: List<Mesh> = emptyList()

    override fun init() {
        renderer.init()
        shader.compile()

        cubeMesh = Mesh.fromFile("src/test/resources/cube.obj")
        roomMeshes = Mesh.fromFile("src/main/resources/textures/scene.obj")
    }

    /**
     * Controls:
     *  - W,A,S,D - move in the x-z-plane
     *  - Q,E - move along the y-axis
     *  - SPACE - teleport to point of intersection
     */
    override fun input(window: Window) {
        direction.x = when {
            window.isKeyPressed(GLFW.GLFW_KEY_D) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_A) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        direction.y = when {
            window.isKeyPressed(GLFW.GLFW_KEY_Q) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_E) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        direction.z = when {
            window.isKeyPressed(GLFW.GLFW_KEY_S) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_W) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }

        mousePosition = Vector2f(window.mousePosition)
        window.mousePosition = Vector2f(0.0F)

        // TODO this is not good enough (boolean switches back and forth really fast)
        if (window.isKeyPressed(GLFW.GLFW_KEY_F)) {
            roomWireframe = !roomWireframe
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE) && cameraRayResult.hasValue()) {
            camera.teleport(cameraRayResult.point)
        }
    }

    override fun update(interval: Float) {
        val tmp = Vector3f(direction).mul(cameraSpeed)
        camera.move(tmp)

        val rot = Vector2f(mousePosition.x, mousePosition.y)
                .mul(mouseSensitivity)
        camera.rotate(rot)

        val cameraDirection = camera.direction()
        val startOffset = Vector3f(cameraDirection)
                .add(0.0F, -0.1F, 0.0F) // move the start down a little
                .normalize()
                .mul(0.01F)
        cameraRayStart = Vector3f(camera.position)
                .add(startOffset)

        cameraRayResult = intersects(cameraRayStart, cameraDirection, roomMeshes, roomTransformation)
    }

    override fun render(window: Window) {
        renderer.clear()
        if (window.isResized) {
            glViewport(0, 0, window.width, window.height)
            window.isResized = false
            val aspectRatio = window.width.toFloat() / window.height.toFloat()
            camera.aspectRatioChanged(aspectRatio)
        }

        shader.apply(camera)
        shader.setUniform("color", Vector4f(1.0F, 1.0F, 1.0F, 1.0F))

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
    }

    private fun renderRoom() {
        renderer.wireframe(roomWireframe) {
            for (roomMesh in roomMeshes) {
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.mesh(shader, roomMesh, roomTransformation, color)
            }
        }
    }

    private fun renderCoordinateSystemAxis() {
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(1.0F, 0.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 1.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 1.0F)).scale(0.1F))

        val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), color)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F), color)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F), color)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 1.0F), Vector3f(1.0F, 0.0F, 0.0F), color)
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), color)
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F), color)
    }

    private fun renderRayFromCamera() {
        val color = Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
        if (cameraRayResult.hasValue()) {
            renderer.line(shader, cameraRayStart, cameraRayResult.point, color)
            renderer.cube(shader, Matrix4f().translate(cameraRayResult.point).scale(0.1F), color)
        } else {
            renderer.line(shader, cameraRayStart, camera.direction().mul(1000.0F), color)
        }
    }
}
