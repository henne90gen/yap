package de.yap.engine.ecs.systems

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.TriggerType
import de.yap.engine.ecs.TriggerTypeComponent
import de.yap.engine.ecs.entities.Entity

class TriggerSystem : ISystem(TriggerTypeComponent::class.java, BoundingBoxComponent::class.java) {

    override fun update(interval: Float, entities: List<Entity>) {
        for (entity in entities) {
            updateEntity(interval, entity)
        }
    }

    private fun updateEntity(interval: Float, entity: Entity) {
        if (entity.getComponent<TriggerTypeComponent>().type == TriggerType.GAME_FINISHED) {
            // ToDo get closest entity of the triggering entity type
            //      in case of collision fire event
            //YapGame.getInstance().entityManager.fireEvent(GameFinishedEvent())
        }
    }
}
