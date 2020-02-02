package de.yap.game

import de.yap.engine.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
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
        if (window.isResized) {
            glViewport(0, 0, window.width, window.height)
            window.isResized = false
            val aspectRatio = window.width.toFloat() / window.height.toFloat()
            camera.aspectRatioChanged(aspectRatio)
        }

        renderer.clear()

        shader.apply(camera)

        renderer.line(shader, camera.position, Vector3f(0.0f, 0.0f, 0.0f))
        renderer.line(shader, camera.position, Vector3f(1.0f, 0.0f, 0.0f))
        renderer.line(shader, camera.position, Vector3f(0.0f, 1.0f, 0.0f))
        renderer.line(shader, camera.position, Vector3f(0.0f, 0.0f, 1.0f))

        renderer.cube(shader, Vector3f(0.0F, 0.0F, 0.0F), 0.5F)
        renderer.cube(shader, Vector3f(1.0F, 0.0F, 0.0F), 0.5F)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.cube(shader, Vector3f(0.0F, 1.0F, 0.0F), 0.5F)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F))
        renderer.cube(shader, Vector3f(0.0F, 0.0F, 1.0F), 0.5F)
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F))
    }
}
