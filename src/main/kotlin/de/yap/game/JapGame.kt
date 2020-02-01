package de.yap.game

import de.yap.engine.IGameLogic
import de.yap.engine.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11


class DummyGame : IGameLogic {
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
            GL11.glViewport(0, 0, window.width, window.height)
            window.isResized = false
        }

        renderQuad(shader)

        // Todo remove if you want to render quads
        window.setClearColor(color, color, color, 0.0f)


        renderer.clear()
    }

    private fun renderQuad(shader: Shader?) {
        shader?.bind()

        val vertices = floatArrayOf(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F)
//        glCreateB

        shader?.unbind()
    }
}
