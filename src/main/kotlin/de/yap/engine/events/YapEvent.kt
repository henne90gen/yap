package de.yap.engine.events

abstract class YapEvent

class InitEvent : YapEvent()

class InputEvent : YapEvent()

class UpdateEvent : YapEvent()

class RenderEvent : YapEvent()
