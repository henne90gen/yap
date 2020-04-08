package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Vector3f
import org.joml.Vector4f

class PlayerEntity(position: Vector3f = Vector3f(0.0F), color: Vector4f = Vector4f(1.0F), hasInput: Boolean) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent())
        val cameraType = if (hasInput) CameraType.FIRST_PERSON else CameraType.THIRD_PERSON
        val offset = if (hasInput) Vector3f(0.0F) else Vector3f(0.0F, 2.0F, 2.0F)
        addComponent(CameraComponent(cameraType, offset = offset, color = color, active = hasInput))
        addComponent(PhysicsComponent())
        val material = YapGame.getInstance().renderer.textureMapMaterial
        addComponent(MeshComponent(MeshUtils.unitCube(material)))
    }
}