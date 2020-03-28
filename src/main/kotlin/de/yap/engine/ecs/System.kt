package de.yap.engine.ecs

abstract class System(vararg components: Class<out Component>) {
    val capability = Capability(*components)

    open fun init() {}
    open fun render(entities: List<Entity>) {}
    open fun update(entities: List<Entity>) {}
}

class RenderSystem : System(PositionComponent::class.java, MeshComponent::class.java) {
    override fun render(entities: List<Entity>) {
        for (entity in entities) {
            val position = entity.getComponent<PositionComponent>()
        }
    }
}
