package de.yap.engine.ecs

import org.joml.Vector2f
import org.joml.Vector3f

abstract class Component

class PositionComponent(var position: Vector3f = Vector3f(0.0F)) : Component()

class RotationComponent(
        var pitch: Float = 0.0F,
        var yaw: Float = 0.0F
) : Component()

class MeshComponent : Component()

class CameraComponent(
        var direction: Vector3f = Vector3f(0.0F),
        var mousePosition: Vector2f = Vector2f(0.0F),
        var active: Boolean = false
//        var projection: Matrix4f = Matrix4f(),
//        var view: Matrix4f = Matrix4f()
) : Component()
