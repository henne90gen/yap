package de.yap.engine.ecs

// iterate over all entities
// iterate over all entities that have multiple components (all the same)
// remove an entity and associated nodes
//   - remove from entityList
//   - find out which NodeTypes correspond to the entity and delete it from those lists in the nodeMap
// add entity
//   - generate id
//   - add components
//   - iterate all nodes and create the ones that have matching components
// add component to entity
//   - check that ComponentType does not exist already
//   - just add component
//   - create nodes that are now possible
// remove component from entity
//   - similar to removing an entity


class EntityManager {
    val entityList: List<Entity> = ArrayList()

    // map from unique component-string to entities
    // the component-string is a "list" of components
    val nodeMap: Map<String, List<Entity>> = LinkedHashMap()
    val systems: List<System> = ArrayList()

    fun init() {
        for (system in systems) {
            system.init()
        }
    }

    private fun getNodeId(nodeClass: Class<out Node>): String {
        val fields = nodeClass.declaredFields
        // TODO use fields that are subclasses of Component to construct unique id
        // TODO sort those fields alphabetically
        return nodeClass.name
    }

    fun render() {
        for (system in systems) {
            val nodeClass = system.getRequiredComponents()
            val key = getNodeId(nodeClass)
            val nodes = nodeMap[key]
            nodes?.let { system.render(it) }
        }
    }

    fun update() {
        for (system in systems) {
            val nodeClass = system.getRequiredComponents()
            val key = getNodeId(nodeClass)
            val nodes = nodeMap[key]
            nodes?.let { system.update(it) }
        }
    }
}
