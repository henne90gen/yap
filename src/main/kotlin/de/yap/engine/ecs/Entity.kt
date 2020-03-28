package de.yap.engine.ecs


class Entity {
    // String == Component.class.name
    val components: Map<String, Component> = LinkedHashMap()

    inline fun <reified T : Component> getComponent(): T {
        return components[T::class.java.name] as T
    }
}
