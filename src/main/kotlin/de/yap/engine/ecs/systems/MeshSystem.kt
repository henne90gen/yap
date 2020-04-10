package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
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

        if (entity.hasComponent<CameraComponent>()) {
            val cameraComponent = entity.getComponent<CameraComponent>()
            if (cameraComponent.active && cameraComponent.type == CameraType.FIRST_PERSON) {
                // don't render the model for an active first person camera
                return
            }
        }

        val transformation = Matrix4f()
                .translate(positionComponent.position)
                .translate(meshComponent.offset)
                .rotate(rotationComponent.yaw, Vector3f(0F, 1F, 0F))
                .rotate(rotationComponent.pitch, Vector3f(0F, 0F, 1F))

        YapGame.getInstance().renderer.mesh(meshComponent.mesh, transformation)
    }
}
