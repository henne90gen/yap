package de.yap.engine

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(private val position: Vector3f, private val rotation: Matrix4f) {

    private val zFar = 1000.0f
    private val zNear = 0.01f
    private var aspectRatio = 1.0f

    var viewMatrix = calcViewMatrix()
    var projectionMatrix = calcProjectionMatrix()

    fun move(offset: Vector3f) {
        position.add(offset)
        viewMatrix = Matrix4f().translate(position).mul(rotation)
    }

    fun aspectRatioChanged(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        projectionMatrix = calcProjectionMatrix()
    }

    private fun calcViewMatrix(): Matrix4f {
        return Matrix4f()
                .translate(position)
                .mul(rotation)
    }

    private fun calcProjectionMatrix(): Matrix4f {
        return Matrix4f()
                .perspective(Math.toRadians(45.0).toFloat(), aspectRatio, zNear, zFar)
    }
}
