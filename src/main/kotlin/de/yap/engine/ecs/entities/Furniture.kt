package de.yap.engine.ecs.entities

import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.mesh.Mesh
import org.joml.Vector3f

class TableEntity(position: Vector3f) : Entity() {

    init {
        addComponent(PositionComponent(position))

        val mesh = Mesh.fromFile("models/table.obj")[0]
        addComponent(MeshComponent(mesh))
    }

}

class ChairEntity(position: Vector3f) : Entity() {

    init {
        addComponent(PositionComponent(position))

        val mesh = Mesh.fromFile("models/chair.obj")[0]
        addComponent(MeshComponent(mesh))
    }

}

class WasteBinEntity(position: Vector3f) : Entity() {

    init {
        addComponent(PositionComponent(position))

        val mesh = Mesh.fromFile("models/waste_bin.obj")[0]
        addComponent(MeshComponent(mesh))
    }
}

class ShoeShelfEntity(position: Vector3f) : Entity() {

    init {
        addComponent(PositionComponent(position))

        val mesh = Mesh.fromFile("models/shoe_shelf.obj")[0]
        addComponent(MeshComponent(mesh))
    }
}

class WardrobeEntity(position: Vector3f) : Entity() {

    init {
        addComponent(PositionComponent(position))

        val mesh = Mesh.fromFile("models/wardrobe.obj")[0]
        addComponent(MeshComponent(mesh))
    }
}