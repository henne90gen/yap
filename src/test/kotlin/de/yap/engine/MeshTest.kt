package de.yap.engine

import org.joml.Vector3f
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MeshTest {
    @Test
    fun testMeshWithNoArgumentsIsEmpty() {
        val m = Mesh()
        assertEquals(0, m.vertices.size)
        assertEquals(0, m.indices.size)
    }

    @Test
    fun testMeshWithQuad() {
        val m = Mesh()
        val newM = m.withQuad(Vector3f(0.0F, 1.0F, 0.0F), Vector3f(), Vector3f(1.0F, 1.0F, 0.0F))
        assertEquals(0, m.vertices.size)
        assertEquals(0, m.indices.size)

        val expectedVertices = listOf(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F)
        assertEquals(expectedVertices, newM.vertices)
        val expectedIndices = listOf(0, 1, 2, 1, 3, 2)
        assertEquals(expectedIndices, newM.indices)
    }

    @Test
    fun testMeshFromNonExistentFile() {
        val m = Mesh.fromFile("non-existent.file")
        assertNull(m)
    }

    @Test
    fun testMeshFromUnsupportedFile() {
        val m = Mesh.fromFile("unsupported.file")
        assertNull(m)
    }

    @Test
    fun testMeshFromObjFile() {
        val m = Mesh.fromFile("src/test/resources/cube.obj")
        assertNotNull(m)

        val expectedVertices = listOf(
                1.0F, -1.0F, -1.0F,
                1.0F, -1.0F, 1.0F,
                -1.0F, -1.0F, 1.0F,
                -1.0F, -1.0F, -1.0F,
                1.0F, 1.0F, -1.0F,
                1.0F, 1.0F, 1.0F,
                -1.0F, 1.0F, 1.0F,
                -1.0F, 1.0F, -1.0F
        )
        assertEquals(expectedVertices, m.vertices)

        val expectedIndices = listOf(
                1, 3, 0,
                7, 5, 4,
                4, 1, 0,
                5, 2, 1,
                2, 7, 3,
                0, 7, 4,
                1, 2, 3,
                7, 6, 5,
                4, 5, 1,
                5, 6, 2,
                2, 6, 7,
                0, 3, 7
        )
        assertEquals(expectedIndices, m.indices)
    }
}
