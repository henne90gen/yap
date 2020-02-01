package de.yap.game

import de.yap.engine.*
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {
    private var direction = 0
    private var color = 0.0f
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")

    @Throws(Exception::class)
    override fun init() {
        renderer.init()
        shader.compile()
    }

    override fun input(window: Window) {
        direction = when {
            window.isKeyPressed(GLFW.GLFW_KEY_UP) -> {
                1
            }
            window.isKeyPressed(GLFW.GLFW_KEY_DOWN) -> {
                -1
            }
            else -> {
                0
            }
        }
    }

    override fun update(interval: Float) {
        color += direction * 0.01f
        if (color > 1) {
            color = 1.0f
        } else if (color < 0) {
            color = 0.0f
        }
    }

    override fun render(window: Window) {
        if (window.isResized) {
            glViewport(0, 0, window.width, window.height)
            window.isResized = false
        }
        renderer.clear()

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
