package de.yap

import org.apache.logging.log4j.LogManager

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
