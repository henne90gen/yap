package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*

open class Entity {
    // String == Component.class.name
    val components: MutableMap<String, Component> = LinkedHashMap()

    inline fun <reified T : Component> getComponent(): T {
        return components[T::class.java.name] as T
    }

    fun addComponent(component: Component) {
        components[component::class.java.name] = component
    }

    inline fun <reified T : Component> hasComponent(): Boolean {
        return T::class.java.name in components
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

