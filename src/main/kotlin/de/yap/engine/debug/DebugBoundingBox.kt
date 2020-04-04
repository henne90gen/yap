package de.yap.engine.debug

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW

class DebugBoundingBox : ISystem(BoundingBoxComponent::class.java, PositionComponent::class.java) {

    var enabled = false

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        entities.forEach(this::renderEntity)
    }

    private fun renderEntity(entity: Entity) {
        YapGame.getInstance().renderer.wireframe {
            val position = entity.getComponent<PositionComponent>().position
            val boundingBox = entity.getComponent<BoundingBoxComponent>()
            val scale = Vector3f(boundingBox.max).sub(boundingBox.min)
            val transformation = Matrix4f()
                    .translate(Vector3f(position).add(Vector3f(0.5F)))
                    .scale(scale)
            val color = Vector4f(1.0F, 0.078431373F, 0.576470588F, 1.0F)
            YapGame.getInstance().renderer.cube(transformation, color)
        }
    }
}
