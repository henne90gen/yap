package de.yap.engine.events

abstract class YapEvent

class InitEvent : YapEvent()

class InputEvent : YapEvent()

data class UpdateEvent(val interval: Float = 0.0F) : YapEvent()

class RenderEvent : YapEvent()

data class KeyboardEvent(
        val key: Int = 0,
        val scancode: Int = 0,
        val action: Int = 0,
        val mods: Int = 0
) : YapEvent()
