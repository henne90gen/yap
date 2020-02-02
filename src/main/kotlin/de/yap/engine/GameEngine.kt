package de.yap.engine

class GameEngine(windowTitle: String?, width: Int, height: Int, vSync: Boolean, private val gameLogic: IGameLogic) : Runnable {

    companion object {
        const val TARGET_FPS = 75
        const val TARGET_UPS = 30
    }

    private val window: Window = Window(windowTitle!!, width, height, vSync)
    private val gameLoopThread: Thread = Thread(this, "GAME_LOOP")
    private val timer: Timer = Timer()

    fun start() {
        val osName = System.getProperty("os.name")
        if (osName.contains("Mac")) {
            gameLoopThread.run()
        } else {
            gameLoopThread.start()
        }
    }

    override fun run() {
        try {
            init()
            gameLoop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        window.init()
        timer.init()
        gameLogic.init()
    }

    private fun gameLoop() {
        var elapsedTime: Float
        var accumulator = 0f
        val interval = 1f / TARGET_UPS
        val running = true
        while (running && !window.windowShouldClose()) {
            elapsedTime = timer.elapsedTime
            accumulator += elapsedTime

            input()

            while (accumulator >= interval) {
                // calculate actual last update time here
                window.setUps(1.0F / interval)
                update(interval)
                accumulator -= interval
            }

            render()
            window.setFps(1.0F / elapsedTime)

            if (!window.isVSync()) {
                sync()
            }
        }
    }

    private fun sync() {
        val loopSlot = 1f / TARGET_FPS
        val endTime: Double = timer.lastLoopTime + loopSlot
        while (timer.time < endTime) {
            try {
                Thread.sleep(1)
            } catch (ie: InterruptedException) {
            }
        }
    }

    private fun input() {
        gameLogic.input(window)
    }

    private fun update(interval: Float) {
        gameLogic.update(interval)
    }

    private fun render() {
        gameLogic.render(window)
        window.update()
    }
}
