package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

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

private val log: Logger = LogManager.getLogger()

fun time(name: String, active: Boolean = true, func: () -> Unit) {
    val startTime = System.nanoTime()
    func()
    if (active) {
        val diff = (System.nanoTime() - startTime) / 1000000.0
        log.info("{} took {}ms", name, diff)
    }
}
