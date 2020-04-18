package de.yap.engine.ecs.entities

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.EntityTypeComponent
import de.yap.engine.ecs.TriggerType
import de.yap.engine.ecs.TriggerTypeComponent
import org.joml.Vector3f

class TriggerEntity(triggerType: TriggerType, entityType: Class<out Entity>, boundingBoxMin: Vector3f, boundingBoxMax: Vector3f) : Entity() {
    init {
        addComponent(TriggerTypeComponent(triggerType))
        addComponent(BoundingBoxComponent(boundingBoxMin, boundingBoxMax))
        addComponent(EntityTypeComponent(entityType))
        // ToDo add last activated component
    }
}