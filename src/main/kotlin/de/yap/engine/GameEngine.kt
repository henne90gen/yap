package de.yap.engine

class GameEngine(windowTitle: String?, width: Int, height: Int, vSync: Boolean, gameLogic: IGameLogic) : Runnable {

    private val window: Window = Window(windowTitle!!, width, height, vSync)
    private val gameLoopThread: Thread = Thread(this, "GAME_LOOP_THREAD")
    private val timer: Timer = Timer()
    private val gameLogic: IGameLogic = gameLogic

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
        } catch (excp: Exception) {
            excp.printStackTrace()
        }
    }

    @Throws(Exception::class)
    protected fun init() {
        window.init()
        timer.init()
        gameLogic.init()
    }

    protected fun gameLoop() {
        var elapsedTime: Float
        var accumulator = 0f
        val interval = 1f / TARGET_UPS
        val running = true
        while (running && !window.windowShouldClose()) {
            elapsedTime = timer.elapsedTime
            accumulator += elapsedTime
            input()
            while (accumulator >= interval) {
                update(interval)
                accumulator -= interval
            }
            render()
            if (!window.isvSync()) {
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

    protected fun input() {
        gameLogic.input(window)
    }

    protected fun update(interval: Float) {
        gameLogic.update(interval)
    }

    protected fun render() {
        gameLogic.render(window)
        window.update()
    }

    companion object {
        const val TARGET_FPS = 75
        const val TARGET_UPS = 30
    }
}
