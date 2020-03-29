package de.yap.engine.ecs

abstract class Event

// TODO split this into key pressed and key released events
data class KeyboardEvent(
        val key: Int = 0,
        val scancode: Int = 0,
        val action: Int = 0,
        val mods: Int = 0
) : Event()

// TODO split this into key pressed and key released events
data class MouseClickEvent(val button: Int = 0, val action: Int = 0, val mods: Int = 0) : Event()

data class MouseMoveEvent(val x: Float = 0.0F, val y: Float = 0.0F) : Event()

data class WindowResizeEvent(val width: Int = 0, val height: Int = 0) : Event()
