package de.yap

import de.yap.engine.GameEngine
import de.yap.game.YapGame
import kotlin.system.exitProcess

class App {
    fun run() {
        try {
            val vSync = true
            val gameLogic = YapGame()
            val gameEngine = GameEngine("YAP", 1280, 720, vSync, gameLogic)
            gameEngine.start()
        } catch (excp: Exception) {
            excp.printStackTrace()
            exitProcess(-1)
        }
    }
}

fun main() {
    App().run()
}
