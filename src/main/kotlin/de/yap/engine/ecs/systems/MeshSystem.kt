package de.yap.engine.ecs.systems

import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.entities.Entity
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f

class MeshSystem : ISystem(PositionComponent::class.java, RotationComponent::class.java, MeshComponent::class.java) {
    override fun render(entities: List<Entity>) {
        entities.forEach(this::renderEntity)
    }

    private fun renderEntity(entity: Entity) {
        val positionComponent = entity.getComponent<PositionComponent>()
        val rotationComponent = entity.getComponent<RotationComponent>()
        val meshComponent = entity.getComponent<MeshComponent>()

        val offset = Vector3f(0.5f, 0.5f, 0.5f)
        val transformation = Matrix4f()
                .translate(positionComponent.position)
                .translate(offset)
                .rotate(rotationComponent.yaw, Vector3f(0F, 1F, 0F))
                .rotate(rotationComponent.pitch, Vector3f(0F, 0F, 1F))
        
        YapGame.getInstance().renderer.mesh(meshComponent.mesh, transformation, meshComponent.color)
    }
}
