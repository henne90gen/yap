package de.yap.engine.ecs.systems

import de.yap.engine.ecs.Capability
import de.yap.engine.ecs.Component
import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity

abstract class ISystem(vararg components: Class<out Component>) {
    val capability = Capability(*components)

    open fun init() {}
    open fun render(entities: List<Entity>) {}
    open fun update(interval: Float, entities: List<Entity>) {}
}

class RenderSystem : ISystem(PositionComponent::class.java, MeshComponent::class.java) {
    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            val position = entity.getComponent<PositionComponent>()
        }
    }
}
