package de.yap.game

import de.yap.engine.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {

    private val log: Logger = LogManager.getLogger(this.javaClass.name)

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")
    private val camera = Camera(Vector3f(0.5F, 0.0F, 3.0F), Matrix4f())
    private val cameraSpeed = 0.1F;

    override fun init() {
        renderer.init()
        shader.compile()
    }

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
    }

    override fun update(interval: Float) {
        val tmp = Vector3f(direction).mul(cameraSpeed)
        camera.move(tmp)
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

        renderRayFromCamera()
        renderCoordinateSystemAxis()
    }

    private fun renderCoordinateSystemAxis() {
        renderer.cube(shader, Vector3f(0.0F, 0.0F, 0.0F), 0.1F)
        renderer.cube(shader, Vector3f(1.0F, 0.0F, 0.0F), 0.1F)
        renderer.cube(shader, Vector3f(0.0F, 1.0F, 0.0F), 0.1F)
        renderer.cube(shader, Vector3f(0.0F, 0.0F, 1.0F), 0.1F)

        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F))

        renderer.line(shader, Vector3f(0.0F, 0.0F, 1.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F))
    }

    private fun renderRayFromCamera() {
        val direction = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(camera.rotation)
        val startOffset = Vector3f(direction.x, direction.y, direction.z)
                .add(0.0F, -0.1F, 0.0F)
                .normalize()
                .mul(0.01F)
        val start = Vector3f(camera.position)
                .add(startOffset)
        val endOffset = Vector3f(direction.x, direction.y, direction.z)
                .mul(10.0F)
        val end = Vector3f(camera.position)
                .add(endOffset)
        renderer.line(shader, start, end)
    }
}
