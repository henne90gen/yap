package de.yap.engine.ecs

import org.joml.Vector3f


open class Entity {
    // String == Component.class.name
    val components: MutableMap<String, Component> = LinkedHashMap()

    inline fun <reified T : Component> getComponent(): T {
        return components[T::class.java.name] as T
    }

    fun addComponent(component: Component) {
        components[component::class.java.name] = component
    }

    fun hasCapability(capability: Capability): Boolean {
        val names = components.keys
        for (component in capability.components) {
            if (!names.contains(component.name)) {
                return false
            }
        }
        return true
    }
}

class PlayerEntity(position: Vector3f = Vector3f(0.0F), hasInput: Boolean) : Entity() {
    init {
        addComponent(PositionComponent(position))
        addComponent(RotationComponent())
        addComponent(MeshComponent())
        addComponent(CameraComponent(active = hasInput))
    }
}
