package de.yap.game

import de.yap.engine.IGameLogic
import de.yap.engine.Renderer
import de.yap.engine.Shader
import de.yap.engine.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer


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

        renderQuad(shader)

        // Todo remove if you want to render quads
//        window.setClearColor(color, color, color, 0.0f)
    }

    private fun renderQuad(shader: Shader?) {
        shader?.bind()

        val vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vertices = floatArrayOf(
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        )

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(vertices.size)
            buffer.put(vertices).flip()

            val vbo = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, buffer!!, GL_STATIC_DRAW)
        } finally {
            if (buffer != null) {
                MemoryUtil.memFree(buffer);
            }
        }

        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_TRIANGLES, 0, 3)

        shader?.unbind()
    }
}
