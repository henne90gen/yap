package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import org.joml.Vector3i
import java.io.File

data class Mesh(val vertices: List<Vector3f> = emptyList(), val indices: List<Vector3i> = emptyList()) {

    fun withQuad(v1: Vector3f, v2: Vector3f, v3: Vector3f): Mesh {
        /*
         v1 +----------+ v3
            |          |
            |          |
         v2 +----------+ v4
        */

        val previousVertexCount = vertices.size
        val newVertices = vertices.toMutableList()
        val newIndices = indices.toMutableList()
        newVertices.add(v1)
        newVertices.add(v2)
        newVertices.add(v3)

        val v4 = Vector3f(v2).add(Vector3f(v3).sub(v1))
        newVertices.add(v4)

        newIndices.add(Vector3i(previousVertexCount + 0, previousVertexCount + 1, previousVertexCount + 2))
        newIndices.add(Vector3i(previousVertexCount + 1, previousVertexCount + 3, previousVertexCount + 2))

        return Mesh(newVertices, newIndices)
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
