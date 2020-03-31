package de.yap.engine.debug

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector4f
import java.time.Duration
import java.time.Instant

class DebugFrameTiming : ISystem() {

    companion object {
        private const val NUMBER_OF_TIMINGS = 5 * 60
        private const val TARGET_FRAME_TIME = 1000.0F / 60.0F
    }

    private var enabled = true
    private var lastTime: Instant = Instant.now()
    private var currentIndex = 0
    private val timings: Array<Duration> = Array(NUMBER_OF_TIMINGS) { Duration.ZERO }

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        val now = Instant.now()
        timings[currentIndex] = Duration.between(lastTime, now)

        lastTime = now
        currentIndex++
        currentIndex %= NUMBER_OF_TIMINGS

        YapGame.getInstance().renderer.inScreenSpace {
            var minimumTiming = Duration.ZERO
            var maximumTiming = Duration.ZERO
            var totalTime = 0L
            for (timingWithIndex in timings.withIndex()) {
                if (timingWithIndex.value < minimumTiming || minimumTiming == Duration.ZERO) {
                    minimumTiming = timingWithIndex.value
                }
                if (timingWithIndex.value > maximumTiming) {
                    maximumTiming = timingWithIndex.value
                }
                totalTime += timingWithIndex.value.toNanos()

                renderTiming(timingWithIndex.index, timingWithIndex.value)
            }
            // TODO maybe print that to the screen
            //      println("Avg: ${totalTime / NUMBER_OF_TIMINGS / 1_000_000.0F} | Min: ${minimumTiming.toNanos() / 1_000_000.0F} | Max: ${maximumTiming.toNanos() / 1_000_000.0F}")
        }
    }

    private fun renderTiming(index: Int, timing: Duration) {
        val aspectRatio = YapGame.getInstance().window.aspectRatio()
        val renderer = YapGame.getInstance().renderer
        val screenWidth = 4 * aspectRatio
        val width = screenWidth / NUMBER_OF_TIMINGS / 2.0F
        val height = timing.toMillis() / TARGET_FRAME_TIME / 3.0F
        val x = -aspectRatio + (index + 0.5F) * width
        val y = -1.0F + height / 2.0F
        val transformation = Matrix4f()
                .translate(x, y, 0.0F)
                .scale(width, height, 0.0F)

        val color = if (index == currentIndex - 1) {
            Vector4f(1.0F, 1.0F, 0.0F, 1.0F)
        } else {
            val red = Vector4f(1.0F, 0.0F, 0.0F, 0.0F)
            val green = Vector4f(0.0F, 1.0F, 0.0F, 0.0F)
            val colorDir = Vector4f(red).sub(green).mul(height - TARGET_FRAME_TIME / 100.0F)
            Vector4f(green).add(colorDir)
        }

        renderer.quad(transformation, color)
    }
}
