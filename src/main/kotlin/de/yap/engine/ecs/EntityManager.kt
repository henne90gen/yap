package de.yap.engine.ecs

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Method
import java.util.concurrent.LinkedBlockingQueue

data class EventListener(val obj: Any, val method: Method)

class Capability(vararg val components: Class<out Component>) {

    companion object {
        val ALL_CAPABILITIES = Capability()
    }

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

abstract class WorkItem

class AddEntityWork(val entity: Entity) : WorkItem()
class AddAllEntitiesWork(val entities: List<Entity>) : WorkItem()
class RemoveEntityWork(val entity: Entity) : WorkItem()
class RemoveAllEntitiesWork : WorkItem()

class EntityManager {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private val capabilityMap: MutableMap<Capability, MutableList<Entity>> = LinkedHashMap()
    private val systems: MutableList<ISystem> = ArrayList()
    private val eventListeners: MutableMap<String, MutableList<EventListener>> = LinkedHashMap()
    private val workQueue = LinkedBlockingQueue<WorkItem>()

    init {
        capabilityMap[Capability.ALL_CAPABILITIES] = ArrayList()
    }

    fun init() {
        for (system in systems) {
            system.init()
        }
    }

    fun render() {
        for (system in systems) {
            val entities = capabilityMap.getOrDefault(system.capability, mutableListOf())
            system.render(entities)
        }
    }

    fun update(interval: Float) {
        while (workQueue.isNotEmpty()) {
            when (val workItem = workQueue.take()) {
                is AddEntityWork -> processAddEntity(workItem)
                is AddAllEntitiesWork -> processAddAllEntities(workItem)
                is RemoveEntityWork -> processRemoveEntity(workItem)
                is RemoveAllEntitiesWork -> processRemoveAllEntities()
            }
        }

        for (system in systems) {
            val entities = capabilityMap.getOrDefault(system.capability, mutableListOf())
            system.update(interval, entities)
        }
    }

    fun registerSystem(system: ISystem) {
        registerEventListener(system)
        systems.add(system)

        val entitiesWithCapability = ArrayList<Entity>()
        for (entity in capabilityMap[Capability.ALL_CAPABILITIES]!!) {
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

    fun getEntities(capability: Capability): List<Entity> {
        return capabilityMap[capability] ?: emptyList()
    }

    fun fireEvent(event: Event) {
        log.trace("Fired $event")

        val listeners = eventListeners[event.javaClass.name]
                ?: return

        for (listener in listeners) {
            listener.method.invoke(listener.obj, event)
        }
    }

    fun addEntity(entity: Entity) {
        workQueue.put(AddEntityWork(entity))
    }

    private fun processAddEntity(work: AddEntityWork) {
        val entity = work.entity
        for (entry in capabilityMap) {
            if (entity.hasCapability(entry.key)) {
                entry.value.add(entity)
            }
        }

        log.debug("Added $entity")
    }

    fun removeEntity(entity: Entity) {
        workQueue.put(RemoveEntityWork(entity))
    }

    private fun processRemoveEntity(work: RemoveEntityWork) {
        val entity = work.entity
        for (capability in capabilityMap) {
            if (!entity.hasCapability(capability.key)) {
                continue
            }
            capability.value.remove(entity)
        }
    }

    fun removeAllEntities() {
        workQueue.put(RemoveAllEntitiesWork())
    }

    private fun processRemoveAllEntities() {
        for (capability in capabilityMap) {
            capability.value.clear()
        }
    }

    fun addAllEntities(entities: List<Entity>) {
        // TODO maybe we can optimize this
        for (entity in entities) {
            addEntity(entity)
        }
    }

    private fun processAddAllEntities(work: AddAllEntitiesWork) {
        // TODO maybe we can optimize this
        val entities = work.entities
        for (entity in entities) {
            addEntity(entity)
        }
    }
}
