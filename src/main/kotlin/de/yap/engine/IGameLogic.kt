package de.yap.engine

interface IGameLogic {
    @Throws(Exception::class)
    fun init(window: Window)

    fun update(interval: Float)
    fun render()
}
