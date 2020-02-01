package de.yap.engine

import org.apache.logging.log4j.LogManager

class Timer {
    var lastLoopTime = 0.0
        private set

    fun init() {
        lastLoopTime = time
    }

    val time: Double
        get() = System.nanoTime() / 1000000000.0

    val elapsedTime: Float
        get() {
            val time = time
            val elapsedTime = (time - lastLoopTime).toFloat()
            lastLoopTime = time
            return elapsedTime
        }
}

private val log = LogManager.getLogger("Timer")

fun time(name: String, func: () -> Unit) {
    val startTime = System.nanoTime()
    func()
    val diff = (System.nanoTime() - startTime) / 1000000
    log.info("{} took {}ms", name, diff)
}

fun timeX(name: String, func: () -> Unit) {
    func()
}
