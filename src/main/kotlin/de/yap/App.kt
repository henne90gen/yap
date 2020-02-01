package de.yap

import de.yap.engine.GameEngine
import de.yap.engine.IGameLogic
import de.yap.game.DummyGame
import kotlin.system.exitProcess

class App {
    fun run() {
        try {
            val vSync = true
            val gameLogic: IGameLogic = DummyGame()
            val gameEng = GameEngine("GAME", 600, 480, vSync, gameLogic)
            gameEng.start()
        } catch (excp: Exception) {
            excp.printStackTrace()
            exitProcess(-1)
        }
    }
}

fun main() {
    App().run()
}
