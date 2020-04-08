package de.yap.engine

import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.systems.CameraSystem
import org.joml.Math.PI
import org.joml.Vector2f
import org.joml.Vector3f
import org.junit.Assert
import org.junit.Test

class CameraSystemTest {

    @Test
    fun testRotate180DegreesWorks() {
        val cameraSystem = CameraSystem()
        val rotationComponent = RotationComponent()
        cameraSystem.rotate(rotationComponent, Vector2f(PI.toFloat(), 0.0F))
        assertVectorsEqual(Vector3f(0.0F, 0.0F, 1.0F), rotationComponent.direction())
    }

    @Test
    fun testRotateXWorks() {
        val cameraSystem = CameraSystem()
        val rotationComponent = RotationComponent()
        cameraSystem.rotate(rotationComponent, Vector2f(PI.toFloat() / 2.0F, 0.0F))
        assertVectorsEqual(Vector3f(1.0F, 0.0F, 0.0F), rotationComponent.direction())
    }

    @Test
    fun testRotateYWorks() {
        val cameraSystem = CameraSystem()
        val rotationComponent = RotationComponent()
        cameraSystem.rotate(rotationComponent, Vector2f(0.0F, PI.toFloat() / 2.0F))
        assertVectorsEqual(Vector3f(0.0F, 1.0F, 0.0F), rotationComponent.direction())
    }

    @Test
    fun testRotatedDirectionIsAlwaysNormalized() {
        val cameraSystem = CameraSystem()
        val rotationComponent = RotationComponent()
        cameraSystem.rotate(rotationComponent, Vector2f(PI.toFloat() / 4.0F, 0.0F))
        assertVectorsEqual(Vector3f(0.5F, 0.0F, -0.5F).normalize(), rotationComponent.direction())
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
