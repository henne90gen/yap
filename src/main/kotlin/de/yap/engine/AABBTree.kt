package de.yap.engine

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import org.joml.Vector3f
import java.lang.Float.max
import java.lang.Float.min
import java.util.*

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

class NodeQueueElement(val node: Node, val distanceSqr: Float) : Comparable<NodeQueueElement> {
    private val comparator = Comparator.comparing { elem: NodeQueueElement -> elem.distanceSqr }
    override fun compareTo(other: NodeQueueElement): Int {
        return comparator.compare(this, other)
    }
}

class EntityQueueElement(val entity: Entity, val distanceSqr: Float) : Comparable<EntityQueueElement> {
    private val comparator = Comparator.comparing { elem: EntityQueueElement -> elem.distanceSqr }
    override fun compareTo(other: EntityQueueElement): Int {
        return comparator.compare(this, other)
    }
}

class AABBTree(entities: List<Entity> = emptyList(), private val splitSize: Int = 10) {
    val root: Node

    init {
        val boundingBox = computeBoundingBox(entities)
        root = buildTree(entities, boundingBox)
    }

    // TODO refactor this (better names, better objects)
    fun get(query: Vector3f, k: Int): List<Entity> {
        val knnList = PriorityQueue<EntityQueueElement>()
        val minQ = PriorityQueue<NodeQueueElement>()
        considerPath(root, query, k, minQ, knnList)
        while (minQ.isNotEmpty()) {
            val current = minQ.poll()
            if (knnList.isNotEmpty() && current.distanceSqr > knnList.peek().distanceSqr) {
                break
            }
            considerPath(current.node, query, k, minQ, knnList)
        }

        return knnList.map { it.entity }
    }

    private fun distanceSqr(boundingBox: BoundingBoxComponent, point: Vector3f): Float {
        val closestPoint = Vector3f(
                min(max(point.x, boundingBox.min.x), boundingBox.max.x),
                min(max(point.y, boundingBox.min.y), boundingBox.max.y),
                min(max(point.z, boundingBox.min.z), boundingBox.max.z)
        )
        return closestPoint.distanceSquared(point)
    }

    private fun considerPath(node: Node, query: Vector3f, k: Int, minQ: PriorityQueue<NodeQueueElement>, knnList: PriorityQueue<EntityQueueElement>) {
        var currentNode = node
        while (currentNode !is LeafNode) {
            val innerNode = currentNode as InnerNode
            val leftDist = distanceSqr(innerNode.left.boundingBox, query)
            val rightDist = distanceSqr(innerNode.right.boundingBox, query)

            currentNode = if (leftDist < rightDist) {
                minQ.add(NodeQueueElement(innerNode.right, rightDist))
                innerNode.left
            } else {
                minQ.add(NodeQueueElement(innerNode.left, leftDist));
                innerNode.right
            }
        }

        for (entity in currentNode.entities) {
            val distanceSqr = query.distanceSquared(entity.getComponent<PositionComponent>().position)
            if (knnList.size == k) {
                if (distanceSqr >= knnList.peek().distanceSqr) {
                    continue
                }
                knnList.remove()
            }
            knnList.add(EntityQueueElement(entity, distanceSqr))
        }
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
