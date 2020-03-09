package de.yap.engine.mesh

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

data class Mesh(
        val vertices: MutableList<Vector3f> = mutableListOf(),
        val texCoords: MutableList<Vector2f> = mutableListOf(),
        val normals: MutableList<Vector3f> = mutableListOf(),
        val indices: MutableList<Vector3i> = mutableListOf(),
        val material: Material? = null
) {

    private var vao: Int? = null

    fun withQuad(v1: Vector3f, v2: Vector3f, v3: Vector3f, texMin: Vector2f, texMax: Vector2f): Mesh {
        /*
         v1 +----------+ v3
            |          |
            |          |
         v2 +----------+ v4
        */

        val preVertCount = vertices.size
        vertices.add(v1)
        vertices.add(v2)
        vertices.add(v3)

        val v4 = Vector3f(v2).add(Vector3f(v3).sub(v1))
        vertices.add(v4)

        texCoords.add(Vector2f(texMin.x, texMin.y)) // v1
        texCoords.add(Vector2f(texMin.x, texMax.y)) // v2
        texCoords.add(Vector2f(texMax.x, texMin.y)) // v3
        texCoords.add(Vector2f(texMax.x, texMax.y)) // v4

        indices.add(Vector3i(preVertCount + 0, preVertCount + 1, preVertCount + 2))
        indices.add(Vector3i(preVertCount + 1, preVertCount + 3, preVertCount + 2))

        return this
    }

    /**
     * Merges the given mesh with this mesh and returns the result.
     * The material of this mesh will be used.
     */
    fun withMesh(mesh: Mesh): Mesh {
        val offset = Vector3i(vertices.size)
        vertices.addAll(mesh.vertices)
        texCoords.addAll(mesh.texCoords)
        for (index in mesh.indices) {
            indices.add(Vector3i(index).add(offset))
        }
        return this
    }

    fun bind() {
        bindVertexData()
        bindMaterial()
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
            MemoryUtil.memFree(buffer)
        }

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0)

        var textCoordsBuffer: FloatBuffer? = null
        try {
            textCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.size * 2)
            textCoordsBuffer.put(texCoords.flatMap { v -> listOf(v.x, v.y) }.toFloatArray()).flip()
            val tbo = GL20.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, tbo)
            GL20.glBufferData(GL20.GL_ARRAY_BUFFER, textCoordsBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(textCoordsBuffer)
        }

        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(1, 2, GL20.GL_FLOAT, false, 0, 0)

        var normalBuffer: FloatBuffer? = null
        try {
            normalBuffer = MemoryUtil.memAllocFloat(normals.size * 3)
            normalBuffer.put(normals.flatMap { v -> listOf(v.x, v.y, v.z) }.toFloatArray()).flip()
            val nbo = GL20.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, nbo)
            GL20.glBufferData(GL20.GL_ARRAY_BUFFER, normalBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(normalBuffer)
        }

        GL20.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(2, 3, GL20.GL_FLOAT, false, 0, 0)

        var indexBuffer: IntBuffer? = null
        try {
            indexBuffer = MemoryUtil.memAllocInt(indices.size * 3)
            indexBuffer.put(indices.flatMap { i -> listOf(i.x, i.y, i.z) }.toIntArray()).flip()

            val ibo = GL15.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo)
            GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(indexBuffer)
        }
    }

    private fun bindMaterial() {
        if (material == null) {
            return
        }

        material.bind()
    }

    fun hasTexture(): Boolean {
        if (material == null) {
            return false
        }
        return material.hasTexture()
    }

    companion object {
        private val log: Logger = LogManager.getLogger(Mesh::class.java.name)

        fun fromFile(filePath: String): List<Mesh> {
            val file = File(filePath)
            if (!file.exists()) {
                log.warn("File '{}' does not exist", filePath)
                return emptyList()
            }

            for (loader in MESH_LOADERS) {
                if (!loader.supports(file)) {
                    continue
                }

                return loader.load(file)
            }

            log.warn("Could not find a suitable loader for '{}'", filePath)
            return emptyList()
        }
    }
}
