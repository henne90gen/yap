package de.yap.engine.ecs

import de.yap.engine.events.Subscribe

interface System {
    fun getRequiredComponents(): List<Class<out Component>>
    fun init() {}
    fun render(entities: List<Entity>) {}
    fun update(entities: List<Entity>) {}
}

class RenderSystem : System {
    override fun getRequiredComponents(): List<Class<out Component>> {
        return listOf(PositionComponent::class.java, MeshComponent::class.java)
    }

    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            entity.getComponent(PositionComponent::class.java)

        }
    }

    @Subscribe(components = { MyComponent.class, ... })
    fun conditionalRender(event: MyEvent) {

    }
}
