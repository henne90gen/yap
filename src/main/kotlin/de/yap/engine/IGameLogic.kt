package de.yap.engine

import de.yap.engine.graphics.Window

interface IGameLogic {
    @Throws(Exception::class)
    fun init(window: Window)

    fun update(interval: Float)
    fun render()
}
