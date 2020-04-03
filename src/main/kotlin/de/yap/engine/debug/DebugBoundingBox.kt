package de.yap.engine.debug

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f

class DebugBoundingBox : ISystem(BoundingBoxComponent::class.java, PositionComponent::class.java) {

    override fun render(entities: List<Entity>) {
        entities.forEach(this::renderEntity)
    }

    private fun renderEntity(entity: Entity) {
        YapGame.getInstance().renderer.wireframe {
            val position = entity.getComponent<PositionComponent>().position
            val boundingBox = entity.getComponent<BoundingBoxComponent>()
            val scale = Vector3f(boundingBox.max).sub(boundingBox.min)
            val transformation = Matrix4f()
                    .translate(position)
                    .scale(scale)
            YapGame.getInstance().renderer.cube(transformation)
        }
    }
}
