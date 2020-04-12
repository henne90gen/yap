package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.Entity
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import kotlin.math.abs

class PathFindingSystem : ISystem(DynamicEntityComponent::class.java, PathComponent::class.java) {
    companion object {
        private const val MIN_DISTANCE_SQ = 0.01F
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

        // render goal
        pathComponent.goal?.let {
            val transform = Matrix4f()
                    .translate(it)
                    .translate(0.5F, 0.5F, 0.5F)
                    .scale(0.5F)
            val goalColor = Vector4f(1.0F, 0.5F, 0.5F, 1.0F)
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
        if (pathComponent.goal == null) {
            return
        }
        if (pathComponent.path.isNotEmpty()) {
            return
        }

        pathComponent.goal?.let { goal ->
            val path = pathComponent.path
            val position = entity.getComponent<PositionComponent>().position
            val currentPosition = Vector3f(position)

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
    }

    private fun followPath(interval: Float, entity: Entity) {
        val pathComponent = entity.getComponent<PathComponent>()
        if (pathComponent.goal == null) {
            return
        }
        if (pathComponent.path.isEmpty()) {
            return
        }

        val positionComponent = entity.getComponent<PositionComponent>()
        val nextWayPoint = pathComponent.path[0]

        val direction = Vector3f(nextWayPoint)
                .sub(positionComponent.position)

        if (direction.lengthSquared() < MIN_DISTANCE_SQ) {
            // we are close enough to the waypoint, we'll go to the next waypoint from here
            pathComponent.path.removeAt(0)
            if (pathComponent.path.isEmpty()) {
                pathComponent.goal = null
            }
        }

        direction.normalize()
                .mul(interval)
        positionComponent.position.add(direction)
    }

    @Subscribe
    fun keyPressed(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_P && event.action == GLFW.GLFW_RELEASE) {
            // TODO do a raycast into the scene and see which block we were pointing at
        }
    }
}
