package de.yap.engine

import org.joml.Vector2f
import org.joml.Vector3f
import org.junit.Assert
import org.junit.Test
import kotlin.math.PI

class CameraTest {

    @Test
    fun testRotate180DegreesWorks() {
        val camera = Camera()
        camera.rotate(Vector2f(PI.toFloat(), 0.0F))
        assertVectorsEqual(Vector3f(0.0F, 0.0F, 1.0F), camera.direction())
    }

    @Test
    fun testRotateXWorks() {
        val camera = Camera()
        camera.rotate(Vector2f(PI.toFloat() / 2.0F, 0.0F))
        assertVectorsEqual(Vector3f(1.0F, 0.0F, 0.0F), camera.direction())
    }

    @Test
    fun testRotateYWorks() {
        val camera = Camera()
        camera.rotate(Vector2f(0.0F, PI.toFloat() / 2.0F))
        assertVectorsEqual(Vector3f(0.0F, 1.0F, 0.0F), camera.direction())
    }

    @Test
    fun testRotatedDirectionIsAlwaysNormalized() {
        val camera = Camera()
        camera.rotate(Vector2f(PI.toFloat() / 4.0F, 0.0F))
        assertVectorsEqual(Vector3f(0.5F, 0.0F, -0.5F).normalize(), camera.direction())
    }

    private fun assertVectorsEqual(v1: Vector3f, v2: Vector3f) {
        val epsilon = 0.0000001F
        val v1Msg = "[" + v1.x + "," + v1.y + "," + v1.z + "]"
        val v2Msg = "[" + v2.x + "," + v2.y + "," + v2.z + "]"
        val message = "$v1Msg vs $v2Msg"
        Assert.assertEquals(message, v1.x, v2.x, epsilon)
        Assert.assertEquals(message, v1.y, v2.y, epsilon)
        Assert.assertEquals(message, v1.z, v2.z, epsilon)
    }
}
