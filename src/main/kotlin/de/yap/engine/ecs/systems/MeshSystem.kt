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

        // ToDo add generic centering the object before rotating
        // ToDo needed since we need to rotate around the mesh center point, this will not work if the mesh is not 1x1x1 cube
        // Not sure why z is negative, maybe it is inverted in blender
        val modelRotationCenter = Vector3f(0.5F, 0.5F, 0.5F)
        val transformation = Matrix4f()
                .translate(positionComponent.position)
                .translate(modelRotationCenter)
                .rotate(rotationComponent.yaw, Vector3f(0F, 1F, 0F))
                .rotate(rotationComponent.pitch, Vector3f(0F, 0F, 1F))
                .translate(-modelRotationCenter.x, -modelRotationCenter.y, -modelRotationCenter.z)

        YapGame.getInstance().renderer.mesh(meshComponent.mesh, transformation, meshComponent.color)
    }
}
