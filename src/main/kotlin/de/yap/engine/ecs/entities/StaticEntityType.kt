package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.Mesh
import de.yap.game.YapGame
import org.joml.Vector3f

open class StaticEntity(id: StaticEntityType, position: Vector3f, pitch: Float = 0.0F, yaw: Float = 0.0F, boundingBox: BoundingBoxComponent = BoundingBoxComponent.unitCube()) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent(pitch, yaw))
        addComponent(MeshComponent(YapGame.getInstance().meshAtlas.meshes[id]!!))
        addComponent(boundingBox)
        addComponent(StaticEntityComponent(id))
    }
}

enum class StaticEntityType(val id: Int, val meshPath: String) {
    // living room
    TABLE(0, "models/table.obj"),
    CHAIR(1, "models/chair.obj"),
    WASTE_BIN(2, "models/waste_bin.obj"),
    TV(3, "models/tv.obj"),
    FLOWER_POT(4, "models/flower_pot.obj"),

    // kitchen
    FRIDGE(5, "models/fridge.obj"),
    OVEN(6, "models/oven.obj"),
    KITCHEN_CABINET(7, "models/kitchen_cabinet.obj"),
    KITCHEN_CABINET_HANGING(8, "models/kitchen_cabinet_hanging.obj"),
    MICROWAVE(9, "models/microwave.obj"),

    // bathroom
    SINK(10, "models/sink.obj"),
    BATHTUB(11, "models/bathtub.obj"),
    SHOWER(12, "models/shower.obj"),

    // bedroom
    SHOE_SHELF(13, "models/shoe_shelf.obj"),
    WARDROBE(14, "models/wardrobe.obj"),
    BED(15, "models/bed.obj"),

    // stuff
    WINDOW(16, "models/window.obj"),
    CLOCK(17, "models/clock.obj"),
    DIGITAL_CLOCK(18, "models/digital_clock.obj"),
    DOOR(19, "models/door.obj"),
}

class MeshAtlas {
    val meshes = LinkedHashMap<StaticEntityType, Mesh>()
    fun init() {
        for (value in StaticEntityType.values()) {
            meshes[value] = Mesh.fromFile(value.meshPath)[0]
        }
    }
}
