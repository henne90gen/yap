package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Vector3f

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
        addComponent(PathComponent(goal))
        addComponent(DynamicEntityComponent(id))
    }
}

enum class DynamicEntityType {
    SIMPLE_AI
}
