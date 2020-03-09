package de.yap.engine

import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
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
        val mesh = Mesh()
        val newMesh = mesh.withQuad(
                Vector3f(0.0F, 1.0F, 0.0F), Vector3f(), Vector3f(1.0F, 1.0F, 0.0F),
                Vector2f(0.0F), Vector2f(1.0F)
        )

        val expectedVertices = listOf(
                Vector3f(0.0F, 1.0F, 0.0F),
                Vector3f(0.0F, 0.0F, 0.0F),
                Vector3f(1.0F, 1.0F, 0.0F),
                Vector3f(1.0F, 0.0F, 0.0F)
        )
        val expectedIndices = listOf(
                Vector3i(0, 1, 2),
                Vector3i(1, 3, 2)
        )

        assertEquals(expectedVertices, mesh.vertices)
        assertEquals(expectedIndices, mesh.indices)

        assertEquals(expectedVertices, newMesh.vertices)
        assertEquals(expectedIndices, newMesh.indices)
    }

    @Test
    fun testMeshFromNonExistentFile() {
        val m = Mesh.fromFile("non-existent.file")
        assertEquals(0, m.size)
    }

    @Test
    fun testMeshFromUnsupportedFile() {
        val m = Mesh.fromFile("unsupported.file")
        assertEquals(0, m.size)
    }

    @Test
    fun testMeshFromObjFile() {
        val m = Mesh.fromFile("src/test/resources/cube.obj")[0]
        assertNotNull(m)
        assertEquals(36, m.vertices.size)

        val expectedIndices = listOf(
                Vector3i(0, 1, 2),
                Vector3i(3, 4, 5),
                Vector3i(6, 7, 8),
                Vector3i(9, 10, 11),
                Vector3i(12, 13, 14),
                Vector3i(15, 16, 17),
                Vector3i(18, 19, 20),
                Vector3i(21, 22, 23),
                Vector3i(24, 25, 26),
                Vector3i(27, 28, 29),
                Vector3i(30, 31, 32),
                Vector3i(33, 34, 35)
        )
        assertEquals(expectedIndices, m.indices)
    }

    @Test
    fun testMeshWithMaterialFromObjFile() {
        val meshes = Mesh.fromFile("src/test/resources/scene.obj")
        assertEquals(4, meshes.size)

        val m1 = meshes[0]
        assertNotNull(m1.material)
    }

    @Test
    fun testWithMesh() {
        val mesh = MeshUtils.quad2D()
        val newMesh = mesh.withMesh(MeshUtils.quad2D(Vector2f(-1.0F), Vector2f(0.0F), Vector2f(0.25F), Vector2f(0.75F)))
        assertNull(newMesh.material)
        val expectedIndices = listOf(
                Vector3i(0, 1, 2),
                Vector3i(0, 3, 1),
                Vector3i(4, 5, 6),
                Vector3i(4, 7, 5)
        )
        assertEquals(expectedIndices, newMesh.indices)
        assertEquals(8, newMesh.vertices.size)
        assertEquals(8, newMesh.texCoords.size)
    }
}
