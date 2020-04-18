package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Vector3f

enum class DynamicEntityType {
    SIMPLE_AI
}

class DynamicEntity(
        id: DynamicEntityType,
        position: Vector3f,
        goal: Vector3f,
        boundingBox: BoundingBoxComponent = BoundingBoxComponent.unitCube()
) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent())

        val material = YapGame.getInstance().renderer.textureMapMaterial
        addComponent(MeshComponent(MeshUtils.unitCube(material)))

        addComponent(boundingBox)

        val pathComponent = PathComponent()
        pathComponent.waypoints.add(goal)
        addComponent(pathComponent)

        addComponent(DynamicEntityComponent(id))
    }
}
