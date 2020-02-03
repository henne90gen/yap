package de.yap.engine

import org.joml.Vector3f

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
}
