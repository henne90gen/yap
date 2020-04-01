package de.yap.engine.ecs.systems

import de.yap.engine.ecs.PhysicsComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import org.apache.logging.log4j.LogManager

class GravitySystem : ISystem(PhysicsComponent::class.java, PositionComponent::class.java) {
    companion object {
        private val log = LogManager.getLogger()
        const val GRAVITY = 9.81F
    }

    override fun update(interval: Float, entities: List<Entity>) {
        for (entity in entities) {
            updateEntity(interval, entity)
        }
    }

    private fun updateEntity(interval: Float, entity: Entity) {
        val positionComponent = entity.getComponent<PositionComponent>()
        val physicsComponent = entity.getComponent<PhysicsComponent>()

        physicsComponent.acceleration += GRAVITY * interval
        physicsComponent.velocity += physicsComponent.acceleration * interval
        positionComponent.position.y -= physicsComponent.velocity * interval

        if (positionComponent.position.y < 1.8F) {
            positionComponent.position.y = 1.8F
        }
    }
}
