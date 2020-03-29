package de.yap.engine.ecs

import org.junit.Test
import kotlin.test.assertEquals

class TestEventListener {
    var eventCounter: Int = 0

    @Subscribe
    fun render(event: WindowResizeEvent) {
        eventCounter++
    }
}

class CustomEvent : Event()

class TestCustomEventListener {
    var eventCounter: Int = 0

    @Subscribe
    fun render(event: CustomEvent) {
        eventCounter++
    }
}

class EventTest {

    @Test
    fun testEventSingleListener() {
        val manager = EntityManager()

        val eventListener = TestEventListener()
        manager.registerEventListener(eventListener)

        manager.fireEvent(WindowResizeEvent())

        assertEquals(1, eventListener.eventCounter)
    }

    @Test
    fun testEventMultipleListenersSameEvent() {
        val manager = EntityManager()

        val eventListener1 = TestEventListener()
        manager.registerEventListener(eventListener1)
        val eventListener2 = TestEventListener()
        manager.registerEventListener(eventListener2)

        manager.fireEvent(WindowResizeEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(1, eventListener2.eventCounter)
    }

    @Test
    fun testEventMultipleListenersDifferentEvents() {
        val manager = EntityManager()

        val eventListener1 = TestEventListener()
        manager.registerEventListener(eventListener1)
        val eventListener2 = TestCustomEventListener()
        manager.registerEventListener(eventListener2)

        manager.fireEvent(WindowResizeEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(0, eventListener2.eventCounter)

        manager.fireEvent(CustomEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(1, eventListener2.eventCounter)
    }

    @Test
    fun testCustomEvent() {
        val manager = EntityManager()

        val eventListener = TestCustomEventListener()
        manager.registerEventListener(eventListener)

        manager.fireEvent(CustomEvent())

        assertEquals(1, eventListener.eventCounter)
    }
}
