package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

class Camera(val position: Vector3f = Vector3f(0.0F)) {

    companion object {
        private val log: Logger = LogManager.getLogger(Camera::class.java.name)
        private val X_AXIS = Vector3f(1.0F, 0.0F, 0.0F)
        private val Y_AXIS = Vector3f(0.0F, 1.0F, 0.0F)
        private val Z_AXIS = Vector3f(0.0F, 0.0F, 1.0F)
    }

    private val zFar = 1000.0f
    private val zNear = 0.01f
    private var aspectRatio = 1.0f

    private var pitch = 0.0F
    private var yaw = 0.0F
    private var roll = 0.0F

    var viewMatrix = calcViewMatrix()
    var projectionMatrix = calcProjectionMatrix()

    fun move(offset: Vector3f) {
        val rotatedOffset = Vector4f(offset.x, offset.y, offset.z, 0.0F)
                .mul(rotationMatrix().invert())

        position.add(Vector3f(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z))
        viewMatrix = calcViewMatrix()
    }

    fun aspectRatioChanged(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        projectionMatrix = calcProjectionMatrix()
    }

    private fun calcViewMatrix(): Matrix4f {
        val pos = Vector3f(position)
        return Matrix4f()
                .mul(rotationMatrix())
                .translate(pos.mul(-1.0f))
    }

    private fun calcProjectionMatrix(): Matrix4f {
        return Matrix4f()
                .perspective(Math.toRadians(45.0).toFloat(), aspectRatio, zNear, zFar)
    }

    fun teleport(point: Vector3f) {
        position.x = point.x
        position.y = point.y
        position.z = point.z
        viewMatrix = calcViewMatrix()
    }

    fun rotate(rot: Vector3f) {
        this.pitch -= rot.y
        this.yaw += rot.x
        this.roll += rot.z
        viewMatrix = calcViewMatrix()
    }

    fun direction(): Vector3f {
        val dir = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(rotationMatrix().invert())
        return Vector3f(dir.x, dir.y, dir.z)
    }

    private fun rotationMatrix(): Matrix4f {
        val result = Matrix4f()
        // order matters!
        result.rotate(roll, Z_AXIS)
        result.rotate(pitch, X_AXIS)
        result.rotate(yaw, Y_AXIS)
        return result
    }
}
