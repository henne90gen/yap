package de.yap.engine.ecs

import de.yap.engine.ecs.entities.DynamicEntityType
import de.yap.engine.ecs.entities.StaticEntityType
import de.yap.engine.graphics.TextureCoords
import de.yap.engine.mesh.Mesh
import de.yap.engine.util.X_AXIS
import de.yap.engine.util.Y_AXIS
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

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
        val offset: Vector3f = Vector3f(0.5F)
) : Component()

enum class CameraType {
    FIRST_PERSON,
    THIRD_PERSON
}

class CameraComponent(
        // the type of camera that is being used
        var type: CameraType,

        // offset to the position of the entity
        var offset: Vector3f = Vector3f(0.0F),

        // current movement direction
        var direction: Vector3f = Vector3f(0.0F),

        // current mouse position for changing the viewing direction
        var mousePosition: Vector2f = Vector2f(0.0F),

        // color of the camera visualization
        val color: Vector4f = Vector4f(1.0F),

        // whether this entity is being controlled by the player
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

class StaticEntityComponent(val id: StaticEntityType) : Component()

class DynamicEntityComponent(val id: DynamicEntityType) : Component()

class PathComponent(
        // the goal for this path
        var waypoints: MutableList<Vector3f> = mutableListOf(),
        var nextWaypoint: Int = 0,
        // the current path that is going to be taken
        val path: MutableList<Vector3f> = mutableListOf()
) : Component()
