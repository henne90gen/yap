package de.yap.engine.ecs.entities

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.mesh.Mesh
import org.joml.Vector3f

open class StaticEntity(position: Vector3f, mesh: Mesh, pitch: Float = 0.0F, yaw: Float = 0.0F, boundingBox: BoundingBoxComponent = BoundingBoxComponent.unitCube()) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent(pitch, yaw))
        addComponent(MeshComponent(mesh))
        addComponent(boundingBox)
    }
}

class TableEntity(position: Vector3f) : StaticEntity(position, Mesh.fromFile("models/table.obj")[0])

class ChairEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/chair.obj")[0], yaw, pitch)

class WasteBinEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/waste_bin.obj")[0], yaw, pitch)

class ShoeShelfEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/shoe_shelf.obj")[0], yaw, pitch)

class WardrobeEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/wardrobe.obj")[0], yaw, pitch)

class FridgeEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/fridge.obj")[0], yaw, pitch)

class OvenEntity(position: Vector3f, yaw: Float = 0.0F, pitch: Float = 0.0F)
    : StaticEntity(position, Mesh.fromFile("models/oven.obj")[0], yaw, pitch)
