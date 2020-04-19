package de.yap.engine.ecs

import de.yap.engine.AABBTree
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.PathFindingSystem
import org.joml.Vector3f
import org.junit.Test
import kotlin.test.assertEquals

class PathFindingTest {

    @Test
    fun testAStarStraightLineNoObstacles() {
        val currentPosition = Vector3f(0.0F, 0.0F, 0.0F)
        val goal = Vector3f(3.0F, 0.0F, 0.0F)
        val path = mutableListOf<Vector3f>()
        val spatialData = AABBTree()

        PathFindingSystem.useAStar(currentPosition, goal, path, spatialData)

        val expectedPath = listOf(
                Vector3f(1.0F, 0.0F, 0.0F),
                Vector3f(2.0F, 0.0F, 0.0F),
                Vector3f(3.0F, 0.0F, 0.0F)
        )
        assertPathsAreEqual(expectedPath, path)
    }

    @Test
    fun testAStarStraightLine() {
        val currentPosition = Vector3f(0.0F, 0.0F, 0.0F)
        val goal = Vector3f(3.0F, 0.0F, 0.0F)
        val path = mutableListOf<Vector3f>()
        val spatialData = AABBTree(listOf(
                entity(1.0F, 0.0F, 0.0F),
                entity(1.0F, 0.0F, 1.0F)
        ))

        PathFindingSystem.useAStar(currentPosition, goal, path, spatialData)

        val expectedPath = listOf(
                Vector3f(0.0F, 0.0F, -1.0F),
                Vector3f(1.0F, 0.0F, -1.0F),
                Vector3f(2.0F, 0.0F, -1.0F),
                Vector3f(3.0F, 0.0F, -1.0F),
                Vector3f(3.0F, 0.0F, 0.0F)
        )
        assertPathsAreEqual(expectedPath, path)
    }

    @Test
    fun testAStarLargeDistance() {
        val currentPosition = Vector3f(0.0F, 0.0F, 0.0F)
        val goal = Vector3f(5.0F, 0.0F, 5.0F)
        val path = mutableListOf<Vector3f>()
        val spatialData = AABBTree()

        PathFindingSystem.useAStar(currentPosition, goal, path, spatialData)

        val expectedPath = listOf(
                Vector3f(1.0F, 0.0F, 0.0F),
                Vector3f(1.0F, 0.0F, 1.0F),
                Vector3f(2.0F, 0.0F, 1.0F),
                Vector3f(3.0F, 0.0F, 1.0F),
                Vector3f(3.0F, 0.0F, 2.0F),
                Vector3f(3.0F, 0.0F, 3.0F),
                Vector3f(3.0F, 0.0F, 4.0F),
                Vector3f(3.0F, 0.0F, 5.0F),
                Vector3f(4.0F, 0.0F, 5.0F),
                Vector3f(5.0F, 0.0F, 5.0F)
        )
        assertPathsAreEqual(expectedPath, path)
    }

    private fun assertPathsAreEqual(expectedPath: List<Vector3f>, path: List<Vector3f>) {
        assertEquals(expectedPath.size, path.size)
        for (i in expectedPath.indices) {
            assertEquals(expectedPath[i], path[i])
        }
    }

    private fun entity(x: Float, y: Float, z: Float): Entity {
        val entity = Entity()
        entity.addComponent(PositionComponent(Vector3f(x, y, z)))
        return entity
    }
}
