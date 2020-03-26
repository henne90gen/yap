package de.yap.engine.events

import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Method

data class Listener(val obj: Any, val method: Method)

class EventBus {

    companion object {
        private val eventBus = EventBus()
        private val log: Logger = LogManager.getLogger(YapGame::class.java.name)

        fun getInstance(): EventBus {
            return eventBus
        }
    }

    private val listeners: MutableMap<String, MutableList<Listener>> = LinkedHashMap()

    fun register(listener: Any) {
        for (method in listener.javaClass.methods) {
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
            val event = eventClass.getDeclaredConstructor().newInstance()
            if (event !is YapEvent) {
                log.error("Could not register event on $method: Parameter must be a subtype of YapEvent")
                continue
            }

            val eventClassStr = eventClass.toString()
            listeners.getOrPut(eventClassStr, ::mutableListOf).add(Listener(listener, method))
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

    fun fire(event: YapEvent) {
        log.debug("Fired $event")
        val eventListeners = listeners[event.javaClass.toString()]
                ?: return

        for (listener in eventListeners) {
            listener.method.invoke(listener.obj, event)
        }
    }
}
