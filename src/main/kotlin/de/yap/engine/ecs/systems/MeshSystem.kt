package de.yap.engine.ecs.systems

import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import de.yap.game.YapGame
import org.joml.Matrix4f

class MeshSystem : ISystem(PositionComponent::class.java, MeshComponent::class.java) {
    override fun render(entities: List<Entity>) {
        entities.forEach(this::renderEntity)
    }

    private fun renderEntity(entity: Entity) {
        val positionComponent = entity.getComponent<PositionComponent>()
        val meshComponent = entity.getComponent<MeshComponent>()

        val transformation = Matrix4f()
                .translate(positionComponent.position)
        YapGame.getInstance().renderer.mesh(meshComponent.mesh, transformation, meshComponent.color)
    }
}
