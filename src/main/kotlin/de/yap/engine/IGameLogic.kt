package de.yap.engine

interface IGameLogic {
    @Throws(Exception::class)
    fun init(window: Window)

    fun input()
    fun update(interval: Float)
    fun render()
}