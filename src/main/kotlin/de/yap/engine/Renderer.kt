package de.yap.engine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Renderer {

    fun init() {
    }

    fun clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun mesh(shader: Shader, mesh: Mesh) {
        shader.bind()

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(mesh.vertices.size)
            buffer.put(mesh.vertices.toFloatArray()).flip()

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
            indexBuffer = MemoryUtil.memAllocInt(mesh.indices.size)
            indexBuffer.put(mesh.indices.toIntArray()).flip()

            val ibo = GL15.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo)
            GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (indexBuffer != null) {
                MemoryUtil.memFree(indexBuffer)
            }
        }

        GL11.glDrawElements(GL_TRIANGLES, mesh.indices.size, GL_UNSIGNED_INT, 0)

        shader.unbind()
    }

    fun quad(shader: Shader) {
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
        this.mesh(shader, mesh)
    }
}
