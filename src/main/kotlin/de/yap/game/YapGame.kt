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
    private var color = 0.0f
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

        shader.setUniform("model", Matrix4f().translate(0.0F, 0.0F, -1.0F))
        shader.apply(camera)

        renderQuadMesh()
        renderer.quad(shader)

        // Todo remove if you want to render quads
        window.setClearColor(color, color, color, 0.0f)
    }

    private fun renderQuadMesh() {
        val vertices = listOf(
                -1.0f, -1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 0.0f
        )
        val indices = listOf(
                0, 1, 2,
                0, 3, 1
        )
        val mesh = Mesh(vertices, indices)
        renderer.mesh(shader, mesh)
    }
}
