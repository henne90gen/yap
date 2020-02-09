package de.yap.engine

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class Camera(val position: Vector3f, private val rotationMatrix: Matrix4f) {

    private val zFar = 1000.0f
    private val zNear = 0.01f
    private var aspectRatio = 1.0f

    var viewMatrix = calcViewMatrix()
    var projectionMatrix = calcProjectionMatrix()

    fun move(offset: Vector3f) {
        position.add(offset)
        viewMatrix = calcViewMatrix()
    }

    fun aspectRatioChanged(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        projectionMatrix = calcProjectionMatrix()
    }

    private fun calcViewMatrix(): Matrix4f {
        val pos = Vector3f(position)
        return Matrix4f()
                .translate(pos.mul(-1.0f))
                .mul(rotationMatrix)
    }

    private fun calcProjectionMatrix(): Matrix4f {
        return Matrix4f()
                .perspective(Math.toRadians(45.0).toFloat(), aspectRatio, zNear, zFar)
    }

    fun teleport(point: Vector3f) {
        position.x = point.x
        position.y = point.y
        position.z = point.z
    }

    fun rotate(newDirection: Vector3f) {
        val dir = Vector3f(0.0F, 0.0F, -1.0F)
        val axis = dir
                .cross(newDirection)
        val angle = dir.angle(newDirection)
        rotationMatrix.rotate(angle, axis.normalize())
    }

    fun direction(): Vector3f {
        val dir = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(rotationMatrix)
        return Vector3f(dir.x, dir.y, dir.z)
    }
}
