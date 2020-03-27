package de.yap.engine.ecs


class Entity {
    // String == Component.class.name
    val components: Map<String, Component> = LinkedHashMap()
}
