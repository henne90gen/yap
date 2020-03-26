package de.yap.engine.events

import org.junit.Test
import kotlin.test.assertEquals

class TestEventListener {
    var eventCounter: Int = 0

    @Subscribe
    fun render(event: RenderEvent) {
        eventCounter++
    }
}

class CustomEvent : YapEvent()

class TestCustomEventListener {
    var eventCounter: Int = 0

    @Subscribe
    fun render(event: CustomEvent) {
        eventCounter++
    }
}

class EventBusTest {

    @Test
    fun testEventBusSingleListener() {
        val bus = EventBus.getInstance()

        val eventListener = TestEventListener()
        bus.register(eventListener)

        bus.fire(RenderEvent())

        assertEquals(1, eventListener.eventCounter)
    }

    @Test
    fun testEventBusMultipleListenersSameEvent() {
        val bus = EventBus.getInstance()

        val eventListener1 = TestEventListener()
        bus.register(eventListener1)
        val eventListener2 = TestEventListener()
        bus.register(eventListener2)

        bus.fire(RenderEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(1, eventListener2.eventCounter)
    }

    @Test
    fun testEventBusMultipleListenersDifferentEvents() {
        val bus = EventBus.getInstance()

        val eventListener1 = TestEventListener()
        bus.register(eventListener1)
        val eventListener2 = TestCustomEventListener()
        bus.register(eventListener2)

        bus.fire(RenderEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(0, eventListener2.eventCounter)

        bus.fire(CustomEvent())

        assertEquals(1, eventListener1.eventCounter)
        assertEquals(1, eventListener2.eventCounter)
    }

    @Test
    fun testCustomEvent() {
        val bus = EventBus.getInstance()

        val eventListener = TestCustomEventListener()
        bus.register(eventListener)

        bus.fire(CustomEvent())

        assertEquals(1, eventListener.eventCounter)
    }
}
