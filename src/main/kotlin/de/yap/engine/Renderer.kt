package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Renderer {

    companion object {
        private val log: Logger = LogManager.getLogger(Renderer::class.java.name)
    }

    fun init() {
        glEnable(GL_DEPTH_TEST)
    }

    fun clear() {
        glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun mesh(shader: Shader, mesh: Mesh, transformation: Matrix4f = Matrix4f()) {
        shader.bind()
        shader.setUniform("model", transformation)

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(mesh.vertices.size * 3)
            buffer.put(mesh.vertices.flatMap { v -> listOf(v.x, v.y, v.z) }.toFloatArray()).flip()

            val vbo = GL20.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo)
            GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (buffer != null) {
                MemoryUtil.memFree(buffer)
            }
        }

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0)

        var indexBuffer: IntBuffer? = null
        try {
            indexBuffer = MemoryUtil.memAllocInt(mesh.indices.size * 3)
            indexBuffer.put(mesh.indices.flatMap { i -> listOf(i.x, i.y, i.z) }.toIntArray()).flip()

            val ibo = GL15.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo)
            GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (indexBuffer != null) {
                MemoryUtil.memFree(indexBuffer)
            }
        }

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        shader.unbind()
    }

    fun quad(shader: Shader, transformation: Matrix4f = Matrix4f()) {
        val vertices = listOf(
                Vector3f(-1.0F, -1.0F, 0.0F),
                Vector3f(1.0F, 1.0F, 0.0F),
                Vector3f(-1.0F, 1.0F, 0.0F),
                Vector3f(1.0F, -1.0F, 0.0F)
        )
        val indices = listOf(
                Vector3i(0, 1, 2),
                Vector3i(0, 3, 1)
        )
        val mesh = Mesh(vertices, indices)
        this.mesh(shader, mesh, transformation)
    }

    fun cube(shader: Shader, transformation: Matrix4f = Matrix4f()) {
        val vertices = listOf(
                // back
                Vector3f(-0.5F, -0.5F, -0.5F), // 0
                Vector3f(0.5F, -0.5F, -0.5F),  // 1
                Vector3f(0.5F, 0.5F, -0.5F),   // 2
                Vector3f(-0.5F, 0.5F, -0.5F),  // 3

                // front
                Vector3f(-0.5F, -0.5F, 0.5F),  // 4
                Vector3f(0.5F, -0.5F, 0.5F),   // 5
                Vector3f(0.5F, 0.5F, 0.5F),    // 6
                Vector3f(-0.5F, 0.5F, 0.5F)    // 7
        )
        val indices = listOf(
                // front
                Vector3i(0, 1, 2),
                Vector3i(0, 2, 3),

                // back
                Vector3i(4, 5, 6),
                Vector3i(4, 6, 7),

                // right
                Vector3i(5, 1, 2),
                Vector3i(5, 2, 6),

                // left
                Vector3i(0, 4, 7),
                Vector3i(0, 7, 3),

                // top
                Vector3i(7, 6, 2),
                Vector3i(7, 2, 3),

                // bottom
                Vector3i(4, 5, 1),
                Vector3i(4, 1, 0)
        )
        val mesh = Mesh(vertices, indices)
        this.mesh(shader, mesh, transformation)
    }

    fun line(shader: Shader, start: Vector3f, end: Vector3f) {
        shader.bind()
        shader.setUniform("model", Matrix4f())

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val vertices = listOf(
                start.x, start.y, start.z,
                end.x, end.y, end.z
        )

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(vertices.size)
            buffer.put(vertices.toFloatArray()).flip()

            val vbo = GL20.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo)
            GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (buffer != null) {
                MemoryUtil.memFree(buffer)
            }
        }

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_LINES, 0, 2)

        shader.unbind()
    }

    fun wireframe(activate: Boolean = true, func: () -> Unit) {
        if (activate) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        }
        func()
        if (activate) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        }
    }
}
