package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.Mesh
import de.yap.game.YapGame
import org.joml.Vector3f

open class StaticEntity(id: StaticEntities, position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F, boundingBox: BoundingBoxComponent = BoundingBoxComponent.unitCube()) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent(pitch, yaw))
        addComponent(MeshComponent(YapGame.getInstance().meshAtlas.meshes[id]!!))
        addComponent(boundingBox)
        addComponent(StaticEntityComponent(id))
    }
}

enum class StaticEntities(val meshPath: String) {
    TABLE("models/table.obj"),
    CHAIR("models/chair.obj"),
    WASTE_BIN("models/waste_bin.obj"),
    SHOE_SHELF("models/shoe_shelf.obj"),
    WARDROBE("models/wardrobe.obj"),
    FRIDGE("models/fridge.obj"),
    OVEN("models/oven.obj"),
    KITCHEN_CABINET("models/kitchen_cabinet.obj"),
    WINDOW("models/window.obj"),
    CLOCK("models/clock.obj"),
    DOOR("models/door.obj"),
    SINK("models/sink.obj"),
    BATHTUB("models/bathtub.obj"),
}

class MeshAtlas {
    val meshes = LinkedHashMap<StaticEntities, Mesh>()
    fun init() {
        for (value in StaticEntities.values()) {
            meshes[value] = Mesh.fromFile(value.meshPath)[0]
        }
    }
}
