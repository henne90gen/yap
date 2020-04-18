package de.yap.engine

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import org.joml.Vector3f
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AABBTreeTest {

    private fun createEntity(x: Float, y: Float, z: Float): Entity {
        val entity = Entity()
        entity.addComponent(PositionComponent(Vector3f(x, y, z)))
        return entity
    }

    @Test
    fun testAABBTree() {
        val entities = listOf(
                createEntity(0.0F, 0.0F, 0.0F),
                createEntity(1.0F, 0.0F, 0.0F),
                createEntity(0.0F, 0.0F, 1.0F),
                createEntity(0.0F, 0.0F, 2.0F)
        )
        val tree = AABBTree(entities, 2)
        assertTrue(tree.root is InnerNode)

        val left = (tree.root as InnerNode).left
        assertTrue(left is LeafNode)
        assertEquals(2, left.entities.size)

        val right = (tree.root as InnerNode).left
        assertTrue(right is LeafNode)
        assertEquals(2, right.entities.size)
    }

    @Test
    fun testAABBTreeCanRetrieve() {
        val entities = listOf(
                createEntity(0.0F, 0.0F, 0.0F),
                createEntity(1.0F, 0.0F, 0.0F),
                createEntity(0.0F, 0.0F, 1.0F),
                createEntity(0.0F, 0.0F, 2.0F)
        )
        val tree = AABBTree(entities, 2)
        val result = tree.get(Vector3f(0.0F, 0.0F, 0.0F), 1)
        assertEquals(1, result.size)

        val entity = result[0]
        assertEquals(Vector3f(0.0F, 0.0F, 0.0F), entity.getComponent<PositionComponent>().position)
    }
}
