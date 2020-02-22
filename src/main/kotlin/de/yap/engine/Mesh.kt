package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.nio.FloatBuffer
import java.nio.IntBuffer

data class Mesh(val vertices: List<Vector3f> = emptyList(), val indices: List<Vector3i> = emptyList(),
                val texture: Texture? = null, val textCoords: List<Vector2f> = emptyList()) {

    private var vao: Int? = null

    fun withQuad(v1: Vector3f, v2: Vector3f, v3: Vector3f): Mesh {
        /*
         v1 +----------+ v3
            |          |
            |          |
         v2 +----------+ v4
        */

        val preVertCount = vertices.size
        val newVertices = vertices.toMutableList()
        val newIndices = indices.toMutableList()
        newVertices.add(v1)
        newVertices.add(v2)
        newVertices.add(v3)

        val v4 = Vector3f(v2).add(Vector3f(v3).sub(v1))
        newVertices.add(v4)

        newIndices.add(Vector3i(preVertCount + 0, preVertCount + 1, preVertCount + 2))
        newIndices.add(Vector3i(preVertCount + 1, preVertCount + 3, preVertCount + 2))

        return Mesh(newVertices, newIndices, null)
    }

    fun bind() {
        bindVertexData()
        bindTextures()
    }

    /**
     * Only uploads the models vertex data once and then reuses the VAO
     * TODO make it possible to change vertex data, while also reusing buffers on the GPU
     */
    private fun bindVertexData() {
        if (vao != null) {
            GL30.glBindVertexArray(vao!!)
            return
        }

        vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao!!)

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(vertices.size * 3)
            buffer.put(vertices.flatMap { v -> listOf(v.x, v.y, v.z) }.toFloatArray()).flip()

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

        if (texture != null) {
            var textCoordsBuffer: FloatBuffer? = null
            try {
                textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.size * 2)
                textCoordsBuffer.put(textCoords.flatMap { v -> listOf(v.x, v.y) }.toFloatArray()).flip()
                val tbo = GL20.glGenBuffers()
                GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, tbo)
                GL20.glBufferData(GL20.GL_ARRAY_BUFFER, textCoordsBuffer!!, GL20.GL_STATIC_DRAW)
            } finally {
                if (buffer != null) {
                    MemoryUtil.memFree(textCoordsBuffer)
                }
            }
        }

        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL20.GL_FLOAT, false, 0, 0)

        var indexBuffer: IntBuffer? = null
        try {
            indexBuffer = MemoryUtil.memAllocInt(indices.size * 3)
            indexBuffer.put(indices.flatMap { i -> listOf(i.x, i.y, i.z) }.toIntArray()).flip()

            val ibo = GL15.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo)
            GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (indexBuffer != null) {
                MemoryUtil.memFree(indexBuffer)
            }
        }
    }

    private fun bindTextures() {
        if (texture == null) {
            return
        }

        texture.bind()
    }

    companion object {
        private val log: Logger = LogManager.getLogger(Mesh::class.java.name)

        fun fromFile(filePath: String): Mesh? {
            val file = File(filePath)
            if (!file.exists()) {
                log.warn("File '{}' does not exist", filePath)
                return null
            }

            for (loader in MESH_LOADERS) {
                if (!loader.supports(file)) {
                    continue
                }

                return loader.load(file)
            }

            log.warn("Could not find a suitable loader for '{}'", filePath)
            return null
        }
    }
}
