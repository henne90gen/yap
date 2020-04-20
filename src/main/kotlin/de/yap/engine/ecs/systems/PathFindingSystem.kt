package de.yap.engine.ecs.systems

import de.yap.engine.AABBTree
import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.util.CollisionUtils
import de.yap.engine.util.TransformedBoundingBox
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.util.*

/**
 * To place a new goal for the dynamic entity to walk to, use the 'P'-key
 */
class PathFindingSystem : ISystem(DynamicEntityComponent::class.java, PathComponent::class.java) {
    companion object {
        // 0.01F * 0.01F
        private const val MIN_DISTANCE_SQ = 0.0001F
        private const val A_STAR_MAX_ITERATIONS = 10_000

        private val COLLISION_CAPABILITY = Capability(BoundingBoxComponent::class.java, PositionComponent::class.java)

        fun useAStar(currentPosition: Vector3f, goal: Vector3f, path: MutableList<Vector3f>, spatialData: AABBTree) {
            class Node(val position: Vector3f, val distance: Double, val estimatedDistance: Double) : Comparable<Node> {
                override fun compareTo(other: Node): Int {
                    return when {
                        estimatedDistance < other.estimatedDistance -> -1
                        estimatedDistance > other.estimatedDistance -> 1
                        else -> 0
                    }
                }
            }

            fun h(position: Vector3f): Double {
                return Vector3f(position).sub(goal).length().toDouble()
            }

            fun findNeighbors(node: Node): List<Node> {
                val positions = listOf(
                        Vector3f(node.position).add(1.0F, 0.0F, 0.0F),
                        Vector3f(node.position).add(-1.0F, 0.0F, 0.0F),
                        Vector3f(node.position).add(0.0F, 0.0F, 1.0F),
                        Vector3f(node.position).add(0.0F, 0.0F, -1.0F)
                )
                val result = mutableListOf<Node>()
                for (position in positions) {
                    val entities = spatialData
                            .get(position, 3)
                            .map { it.getComponent<PositionComponent>().position }
                    if (!entities.contains(position)) {
                        val distance = node.distance + Vector3f(position).sub(node.position).length()
                        val estimatedDistance = distance + h(position)
                        val neighbor = Node(position, distance, estimatedDistance)
                        result.add(neighbor)
                    }
                }
                return result
            }

            val startNode = Node(currentPosition, 0.0, h(currentPosition))

            val openSet = PriorityQueue<Node>()
            openSet.add(startNode)
            val cameFrom = LinkedHashMap<Node, Node>()
            val gScore = LinkedHashMap<Node, Double>()
            gScore[startNode] = 0.0

            var iterations = 0
            while (openSet.isNotEmpty()) {
                iterations++
                var currentNode = openSet.first()
                if (currentNode.position == goal) {
                    path.add(0, currentNode.position)
                    while (cameFrom.containsKey(currentNode)) {
                        currentNode = cameFrom[currentNode]
                        if (currentNode.position != currentPosition) {
                            path.add(0, currentNode.position)
                        }
                    }
                    return
                }

                openSet.remove(currentNode)
                for (neighbor in findNeighbors(currentNode)) {
                    if (!gScore.containsKey(neighbor) || neighbor.estimatedDistance < gScore[neighbor]!!) {
                        cameFrom[neighbor] = currentNode
                        gScore[neighbor] = neighbor.distance
                        if (!openSet.contains(neighbor)) {
                            openSet.add(neighbor)
                        }
                    }
                }
            }
        }
    }

    init {
        YapGame.getInstance().entityManager.registerCapability(COLLISION_CAPABILITY)
    }

    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            renderPath(entity)
        }
    }

    private fun renderPath(entity: Entity) {
        val pathComponent = entity.getComponent<PathComponent>()
        val renderer = YapGame.getInstance().renderer
        val color = Vector4f(0.5F, 1.0F, 0.5F, 1.0F)
        renderer.wireframe {
            // render path
            var index = pathComponent.path.size
            for (pos in pathComponent.path) {
                index--
                if (index == 0) {
                    // don't render the last path element, it is the goal
                    break
                }

                val transform = Matrix4f()
                        .translate(pos)
                        .translate(0.5F, 0.5F, 0.5F)
                        .scale(0.5F)
                renderer.cube(transform, color)
            }
        }

        // render current goal
        try {
            val last = pathComponent.path.last()
            val transform = Matrix4f()
                    .translate(last)
                    .translate(0.5F, 0.5F, 0.5F)
                    .scale(0.5F)
            val goalColor = Vector4f(1.0F, 0.5F, 0.5F, 1.0F)
            renderer.cube(transform, goalColor)
        } catch (e: NoSuchElementException) {
            // ignore
        }

        // render queued goals
        for (goal in pathComponent.waypoints) {
            val transform = Matrix4f()
                    .translate(goal)
                    .translate(0.5F, 0.5F, 0.5F)
                    .scale(0.5F)
            val goalColor = Vector4f(1.0F, 0.25F, 0.25F, 1.0F)
            renderer.cube(transform, goalColor)
        }
    }

    override fun update(interval: Float, entities: List<Entity>) {
        for (entity in entities) {
            calculatePath(entity)
            followPath(interval, entity)
        }
    }

    private fun calculatePath(entity: Entity) {
        val pathComponent = entity.getComponent<PathComponent>()
        if (pathComponent.waypoints.isEmpty()) {
            return
        }
        if (pathComponent.path.isNotEmpty()) {
            return
        }

        val goal = pathComponent.waypoints[pathComponent.nextWaypoint]
        pathComponent.nextWaypoint++
        pathComponent.nextWaypoint %= pathComponent.waypoints.size

        val path = pathComponent.path
        val position = entity.getComponent<PositionComponent>().position
        val currentPosition = Vector3f(position)

        val spatialData = YapGame.getInstance().entityManager.spatialData
        useAStar(currentPosition, goal, path, spatialData)
//        usePrimitiveAlgorithm(currentPosition, goal, path)
    }

    private fun usePrimitiveAlgorithm(currentPosition: Vector3f, goal: Vector3f, path: MutableList<Vector3f>) {
        // go in x direction first
        if (currentPosition.x < goal.x) {
            while (currentPosition.x < goal.x) {
                currentPosition.x += 1.0F
                path.add(Vector3f(currentPosition))
            }
        } else {
            while (currentPosition.x > goal.x) {
                currentPosition.x -= 1.0F
                path.add(Vector3f(currentPosition))
            }
        }

        // then go in z direction
        if (currentPosition.z < goal.z) {
            while (currentPosition.z < goal.z) {
                currentPosition.z += 1.0F
                path.add(Vector3f(currentPosition))
            }
        } else {
            while (currentPosition.z > goal.z) {
                currentPosition.z -= 1.0F
                path.add(Vector3f(currentPosition))
            }
        }
    }

    private fun followPath(interval: Float, entity: Entity) {
        val pathComponent = entity.getComponent<PathComponent>()
        if (pathComponent.path.isEmpty()) {
            return
        }

        val positionComponent = entity.getComponent<PositionComponent>()
        val nextWayPoint = pathComponent.path[0]

        val direction = Vector3f(nextWayPoint)
                .sub(positionComponent.position)

        if (direction.lengthSquared() < MIN_DISTANCE_SQ) {
            // we are close enough to the waypoint, remove it and go to the next one
            pathComponent.path.removeAt(0)
        }

        if (pathComponent.path.isEmpty()) {
            positionComponent.position = nextWayPoint
        } else {
            direction.normalize()
                    .mul(interval)
            positionComponent.position.add(direction)
        }
    }

    @Subscribe
    fun keyPressed(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_P && event.action == GLFW.GLFW_RELEASE) {
            addNewGoal()
        }
    }

    private fun addNewGoal() {
        val boundingBoxes = YapGame.getInstance().entityManager.getEntities(Capability.ALL_CAPABILITIES)
                .filter { it.hasComponent<PositionComponent>() && it.hasComponent<BoundingBoxComponent>() }
                .map {
                    val position = it.getComponent<PositionComponent>().position
                    TransformedBoundingBox(
                            it.getComponent(),
                            Matrix4f().translate(position)
                    )
                }

        val intersectionResult = CollisionUtils.rayCastFromCamera(boundingBoxes)
        if (!intersectionResult.hasValue()) {
            return
        }

        val pathFindingEntities = YapGame.getInstance().entityManager.getEntities(capability)
        if (pathFindingEntities.isEmpty()) {
            return
        }

        val entity = pathFindingEntities[0]
        val pathComponent = entity.getComponent<PathComponent>()
        val nextGoal = Vector3f(intersectionResult.point)
                .add(intersectionResult.normal)
        pathComponent.waypoints.add(nextGoal)
    }
}
