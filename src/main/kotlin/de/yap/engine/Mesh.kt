package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3f
import java.io.File

// TODO use List<Vector3f> instead of List<Float>
data class Mesh(val vertices: List<Float> = emptyList(), val indices: List<Int> = emptyList()) {

    fun withQuad(v1: Vector3f, v2: Vector3f, v3: Vector3f): Mesh {
        /*
         v1 +----------+ v3
            |          |
            |          |
         v2 +----------+ v4
        */

        // ATTENTION: assuming that vertices are always 3 floats wide
        val previousVertexCount = vertices.size / 3
        val newVertices = vertices.toMutableList()
        val newIndices = indices.toMutableList()
        newVertices.add(v1.x)
        newVertices.add(v1.y)
        newVertices.add(v1.z)
        newVertices.add(v2.x)
        newVertices.add(v2.y)
        newVertices.add(v2.z)
        newVertices.add(v3.x)
        newVertices.add(v3.y)
        newVertices.add(v3.z)

        val v4 = Vector3f(v2).add(Vector3f(v3).sub(v1))
        newVertices.add(v4.x)
        newVertices.add(v4.y)
        newVertices.add(v4.z)

        newIndices.add(previousVertexCount + 0)
        newIndices.add(previousVertexCount + 1)
        newIndices.add(previousVertexCount + 2)

        newIndices.add(previousVertexCount + 1)
        newIndices.add(previousVertexCount + 3)
        newIndices.add(previousVertexCount + 2)

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
