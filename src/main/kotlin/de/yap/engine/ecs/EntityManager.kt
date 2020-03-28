package de.yap.engine.ecs

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Method

data class EventListener(val obj: Any, val method: Method)

class Capability(vararg val components: Class<out Component>) {
    private val id: String = components.map { it.name }.sorted().joinToString(separator = "-")

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Capability

        if (id != other.id) return false

        return true
    }
}

class EntityManager {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private val entityList: MutableList<Entity> = ArrayList()
    private val capabilityMap: MutableMap<Capability, MutableList<Entity>> = LinkedHashMap()
    private val systems: MutableList<System> = ArrayList()
    private val eventListeners: MutableMap<String, MutableList<EventListener>> = LinkedHashMap()

    fun init() {
        for (system in systems) {
            system.init()
        }
    }

    fun render() {
        for (system in systems) {
            val entities = capabilityMap.getOrDefault(system.capability, emptyList<Entity>())
            system.render(entities)
        }
    }

    fun update() {
        for (system in systems) {
            val entities = capabilityMap.getOrDefault(system.capability, emptyList<Entity>())
            system.update(entities)
        }
    }

    fun register(system: System) {
        systems.add(system)

        val entitiesWithCapability = ArrayList<Entity>()
        for (entity in entityList) {
            if (entity.hasCapability(system.capability)) {
                entitiesWithCapability.add(entity)
            }
        }

        capabilityMap[system.capability] = entitiesWithCapability

        log.debug("Registered $system")
    }

    fun registerEventListener(obj: Any) {
        for (method in obj.javaClass.methods) {
            if (!isEventSubscription(method)) {
                continue
            }

            if (method.parameters.isEmpty()) {
                log.error("Could not register event on $method: No parameters provided")
                continue
            }

            if (method.parameters.size > 1) {
                log.warn("Ignoring extra parameters for event registration on $method")
            }

            val eventClass = method.parameters[0].type
            val constructor = try {
                eventClass.getDeclaredConstructor()
            } catch (e: NoSuchMethodException) {
                log.error("The event '$eventClass' does not provide a default constructor. (Add default parameters to all arguments)")
                null
            } ?: continue

            val event = constructor.newInstance()
            if (event !is Event) {
                log.error("Could not register event on $method: Parameter must be a subtype of YapEvent")
                continue
            }

            val eventClassStr = eventClass.name
            eventListeners.getOrPut(eventClassStr, ::mutableListOf).add(EventListener(obj, method))
            log.debug("Registered '$method' as a listener for '$eventClassStr'")
        }
    }

    private fun isEventSubscription(method: Method): Boolean {
        for (annotation in method.annotations) {
            if (annotation is Subscribe) {
                return true
            }
        }
        return false
    }

    fun addEntity(entity: Entity) {
        entityList.add(entity)

        for (entry in capabilityMap) {
            if (entity.hasCapability(entry.key)) {
                entry.value.add(entity)
            }
        }

        log.debug("Added $entity")
    }

    fun fireEvent(event: Event) {
        log.trace("Fired $event")

        val listeners = eventListeners[event.javaClass.name]
                ?: return

        for (listener in listeners) {
            listener.method.invoke(listener.obj, event)
        }
    }
}
