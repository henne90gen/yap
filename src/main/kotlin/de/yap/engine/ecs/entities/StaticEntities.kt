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
    // living room
    TABLE("models/table.obj"),
    CHAIR("models/chair.obj"),
    WASTE_BIN("models/waste_bin.obj"),
    TV("models/tv.obj"),
    FLOWER_POT("models/flower_pot.obj"),

    // kitchen
    FRIDGE("models/fridge.obj"),
    OVEN("models/oven.obj"),
    KITCHEN_CABINET("models/kitchen_cabinet.obj"),
    KITCHEN_CABINET_HANGING("models/kitchen_cabinet_hanging.obj"),

    // bathroom
    SINK("models/sink.obj"),
    BATHTUB("models/bathtub.obj"),
    SHOWER("models/shower.obj"),

    // bedroom
    SHOE_SHELF("models/shoe_shelf.obj"),
    WARDROBE("models/wardrobe.obj"),
    BED("models/bed.obj"),

    // stuff
    WINDOW("models/window.obj"),
    CLOCK("models/clock.obj"),
    DIGITAL_CLOCK("models/digital_clock.obj"),
    DOOR("models/door.obj"),

}

class MeshAtlas {
    val meshes = LinkedHashMap<StaticEntities, Mesh>()
    fun init() {
        for (value in StaticEntities.values()) {
            meshes[value] = Mesh.fromFile(value.meshPath)[0]
        }
    }
}
