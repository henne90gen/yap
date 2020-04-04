package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.Mesh
import org.joml.Vector3f

open class StaticEntity(position: Vector3f, id: StaticEntities, mesh: Mesh, pitch: Float = 0.0F, yaw: Float = 0.0F, boundingBox: BoundingBoxComponent = BoundingBoxComponent.unitCube()) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent(pitch, yaw))
        addComponent(MeshComponent(mesh))
        addComponent(boundingBox)
        addComponent(StaticEntityComponent(id))
    }
}

enum class StaticEntities {
    TABLE,
    CHAIR,
    WASTE_BIN,
    SHOE_SHELF,
    WARDROBE,
    FRIDGE,
    OVEN,
    KITCHEN_CABINET,
    WINDOW,
    CLOCK
}

class TableEntity(position: Vector3f) : StaticEntity(position, StaticEntities.TABLE, Mesh.fromFile("models/table.obj")[0])

class ChairEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.CHAIR, Mesh.fromFile("models/chair.obj")[0], pitch, yaw)

class WasteBinEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.WASTE_BIN, Mesh.fromFile("models/waste_bin.obj")[0], pitch, yaw)

class ShoeShelfEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.SHOE_SHELF, Mesh.fromFile("models/shoe_shelf.obj")[0], pitch, yaw)

class WardrobeEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.WARDROBE, Mesh.fromFile("models/wardrobe.obj")[0], pitch, yaw)

class FridgeEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.FRIDGE, Mesh.fromFile("models/fridge.obj")[0], pitch, yaw)

class OvenEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.OVEN, Mesh.fromFile("models/oven.obj")[0], pitch, yaw)

class KitchenCabinetEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.KITCHEN_CABINET, Mesh.fromFile("models/kitchen_cabinet.obj")[0], pitch, yaw)

class WindowEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.WINDOW, Mesh.fromFile("models/window.obj")[0], pitch, yaw)

class ClockEntity(position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F)
    : StaticEntity(position, StaticEntities.CLOCK, Mesh.fromFile("models/clock.obj")[0], pitch, yaw)
