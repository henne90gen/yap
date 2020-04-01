package de.yap.engine.ecs.entities

import de.yap.engine.ecs.CameraComponent
import de.yap.engine.ecs.PhysicsComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import org.joml.Vector3f
import org.joml.Vector4f

class PlayerEntity(position: Vector3f = Vector3f(0.0F), color: Vector4f = Vector4f(1.0F), hasInput: Boolean) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent())
        addComponent(CameraComponent(color = color, active = hasInput))
        addComponent(PhysicsComponent())
    }
}