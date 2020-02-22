package de.yap.engine

import org.joml.Vector3f
import org.joml.Vector3i
import org.junit.Test

class HalfEdgeMeshTest {

    @Test
    fun testFaceHalfEdgeTraversal() {
        val vertices = listOf(
                Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), Vector3f(1.0F, 1.0F, 0.0F)
        )
        val indices = listOf(Vector3i(0, 1, 2))
        val mesh = HalfEdgeMesh.create(vertices, indices)
//        for (face in mesh.faceHalfEdges(0)) {
//
//        }
    }

    @Test
    fun testFaceVertexTraversal() {
        val vertices = listOf(
                Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), Vector3f(1.0F, 1.0F, 0.0F)
        )
        val indices = listOf(Vector3i(0,1,2))
        val mesh = HalfEdgeMesh.create(vertices, indices)
        for (vertex in mesh.faceVertices(0)) {
//            assertEquals(Vector3f(), vertex.position)
        }
    }
}
