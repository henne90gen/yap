package de.yap.game

import de.yap.engine.*
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {
    private var directionZ = 0.0F
    private var directionY = 0.0F
    private var directionX = 0.0F
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")
    private val camera = Camera(Vector3f(), Matrix4f())

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
        shader.compile()
    }

    override fun input(window: Window) {
        directionX = when {
            window.isKeyPressed(GLFW.GLFW_KEY_A) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_D) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        directionY = when {
            window.isKeyPressed(GLFW.GLFW_KEY_E) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_Q) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        directionZ = when {
            window.isKeyPressed(GLFW.GLFW_KEY_W) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_S) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
    }

    override fun update(interval: Float) {
        camera.move(Vector3f(directionX, 0.0F, 0.0F))
        camera.move(Vector3f(0.0F, directionY, 0.0F))
        camera.move(Vector3f(0.0F, 0.0F, directionZ))
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

        renderer.cube(shader, Vector3f(0.0F, 0.0F, -1.0F))
    }
}
