package de.yap.engine

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import org.joml.Vector3f

abstract class Node(val boundingBox: BoundingBoxComponent)

class LeafNode(
        val entities: List<Entity>,
        boundingBox: BoundingBoxComponent
) : Node(boundingBox)

class InnerNode(
        val left: Node,
        val right: Node,
        boundingBox: BoundingBoxComponent
) : Node(boundingBox)

class AABBTree(entities: List<Entity>, private val splitSize: Int = 10) {
    val root: Node

    init {
        val boundingBox = computeBoundingBox(entities)
        root = buildTree(entities, boundingBox)
    }

    private fun buildTree(entities: List<Entity>, boundingBox: BoundingBoxComponent): Node {
        if (entities.size <= splitSize) {
            return LeafNode(entities, boundingBox)
        }

        val extents = Vector3f(boundingBox.max).sub(boundingBox.min)
        var axis = 0
        var maxExtent = extents[axis]
        if (extents.y > maxExtent) {
            axis = 1
            maxExtent = extents[axis]
        }
        if (extents.z > maxExtent) {
            axis = 2
        }

        val leftEntities = mutableListOf<Entity>()
        val rightEntities = mutableListOf<Entity>()

        val sortedEntities = entities.sortedBy { e -> e.getComponent<PositionComponent>().position[axis] }
        val middleIndex: Int = sortedEntities.size / 2
        for (entityIndex in sortedEntities.indices) {
            if (entityIndex < middleIndex) {
                leftEntities.add(sortedEntities[entityIndex])
            } else {
                rightEntities.add(sortedEntities[entityIndex])
            }
        }

        val leftBoundingBox = computeBoundingBox(leftEntities)
        val left = buildTree(leftEntities, leftBoundingBox)

        val rightBoundingBox = computeBoundingBox(rightEntities)
        val right = buildTree(rightEntities, rightBoundingBox)

        return InnerNode(left, right, boundingBox)
    }

    fun get(position: Vector3f) {

    }

    private fun computeBoundingBox(entities: List<Entity>): BoundingBoxComponent {
        if (entities.isEmpty()) {
            return BoundingBoxComponent(Vector3f(0.0F), Vector3f(0.0F))
        }

        val firstPosition = entities[0].getComponent<PositionComponent>().position
        val min = Vector3f(firstPosition)
        val max = Vector3f(firstPosition)
        for (entity in entities) {
            val position = entity.getComponent<PositionComponent>().position
            if (position.x < min.x) {
                min.x = position.x
            }
            if (position.y < min.y) {
                min.y = position.y
            }
            if (position.z < min.z) {
                min.z = position.z
            }

            if (position.x > max.x) {
                max.x = position.x
            }
            if (position.y > max.y) {
                max.y = position.y
            }
            if (position.z > max.z) {
                max.z = position.z
            }
        }
        return BoundingBoxComponent(min, max)
    }
}
