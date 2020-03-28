package de.yap.engine.ecs

abstract class Event

data class KeyboardEvent(
        val key: Int = 0,
        val scancode: Int = 0,
        val action: Int = 0,
        val mods: Int = 0
) : Event()

data class MouseEvent(val button: Int = 0) : Event()

data class WindowResizeEvent(val width: Int = 0, val height: Int = 0) : Event()
