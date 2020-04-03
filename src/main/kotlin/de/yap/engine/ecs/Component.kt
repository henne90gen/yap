package de.yap.engine.ecs

import de.yap.engine.graphics.TextureCoords
import de.yap.engine.mesh.Mesh
import de.yap.engine.util.X_AXIS
import de.yap.engine.util.Y_AXIS
import org.joml.*

abstract class Component

class PositionComponent(
        var position: Vector3f = Vector3f(0.0F)
) : Component()

class RotationComponent(
        var pitch: Float = 0.0F,
        var yaw: Float = 0.0F
) : Component() {
    fun rotationMatrix(): Matrix4f {
        return Matrix4f()
                .rotate(pitch, X_AXIS)
                .rotate(yaw, Y_AXIS)
    }

    fun direction(): Vector3f {
        val dir = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(rotationMatrix().invert())
        return Vector3f(dir.x, dir.y, dir.z).normalize()
    }
}

class MeshComponent(
        var mesh: Mesh,
        val color: Vector4f = Vector4f(1.0F)
) : Component()

class CameraComponent(
        var direction: Vector3f = Vector3f(0.0F),
        var mousePosition: Vector2f = Vector2f(0.0F),
        val color: Vector4f = Vector4f(1.0F),
        var active: Boolean = false
) : Component()

class PhysicsComponent(
        var velocity: Float = 0.0F,
        var acceleration: Float = 0.0F
) : Component()

class BoundingBoxComponent(
        var min: Vector3f,
        var max: Vector3f
) : Component() {
    companion object {
        fun unitCube(): BoundingBoxComponent {
            return BoundingBoxComponent(Vector3f(0.0F), Vector3f(1.0F))
        }
    }
}

class TextureAtlasIndexComponent(val textureCoords: TextureCoords) : Component()
