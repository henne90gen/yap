package de.yap.engine

import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import java.lang.Float.max
import kotlin.math.PI
import kotlin.math.abs

class Camera(val position: Vector3f = Vector3f(0.0F)) {

    companion object {
        private val log: Logger = LogManager.getLogger(Camera::class.java.name)
        private val X_AXIS = Vector3f(1.0F, 0.0F, 0.0F)
        private val Y_AXIS = Vector3f(0.0F, 1.0F, 0.0F)
        private val Z_AXIS = Vector3f(0.0F, 0.0F, 1.0F)
    }

    enum class UpDirection {
        X_AXIS, Y_AXIS, Z_AXIS,
        NEG_X_AXIS, NEG_Y_AXIS, NEG_Z_AXIS;
    }

    private val zFar = 1000.0f
    private val zNear = 0.01f
    private var aspectRatio = 1.0f

    private var pitch = 0.0F
    private var yaw = 0.0F

    private val movementSpeed = 0.1F
    private val mouseSensitivity = 0.5F

    private var upDirection = UpDirection.Y_AXIS

    var viewMatrix = calcViewMatrix()
    var projectionMatrix = calcProjectionMatrix()

    private fun move(offset: Vector3f) {
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

    fun teleport(point: Vector3f, normal: Vector3f) {
        position.x = point.x
        position.y = point.y
        position.z = point.z
        upDirection = getUpDirection(normal)
        viewMatrix = calcViewMatrix()
    }

    fun rotate(rot: Vector2f) {
        this.pitch -= rot.y
        this.yaw += rot.x
        viewMatrix = calcViewMatrix()
    }

    fun direction(): Vector3f {
        val dir = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(rotationMatrix().invert())
        return Vector3f(dir.x, dir.y, dir.z)
    }

    private fun rotationMatrix(): Matrix4f {
        return when (upDirection) {
            UpDirection.X_AXIS -> {
                Matrix4f()
                        .rotate(PI.toFloat() / 2.0F, Z_AXIS)
                        .rotate(-pitch, Y_AXIS)
                        .rotate(yaw, X_AXIS)
            }
            UpDirection.Y_AXIS -> {
                Matrix4f()
                        .rotate(pitch, X_AXIS)
                        .rotate(yaw, Y_AXIS)
            }
            UpDirection.Z_AXIS -> {
                Matrix4f()
                        .rotate(pitch, X_AXIS)
                        .rotate(yaw, Z_AXIS)
            }
            UpDirection.NEG_X_AXIS -> {
                Matrix4f()
                        .rotate(-PI.toFloat() / 2.0F, Z_AXIS)
                        .rotate(pitch, Y_AXIS)
                        .rotate(-yaw, X_AXIS)
            }
            UpDirection.NEG_Y_AXIS -> {
                Matrix4f()
                        .rotate(PI.toFloat(), Z_AXIS)
                        .rotate(-pitch, X_AXIS)
                        .rotate(-yaw, Y_AXIS)
            }
            UpDirection.NEG_Z_AXIS -> {
                Matrix4f()
                        .rotate(pitch, X_AXIS)
                        .rotate(-yaw, Z_AXIS)
            }
        }
    }

    private fun getUpDirection(normalDirection: Vector3f): UpDirection {
        val angleToX = normalDirection.dot(X_AXIS)
        val angleToY = normalDirection.dot(Y_AXIS)
        val angleToZ = normalDirection.dot(Z_AXIS)

        val absAngleToX = abs(angleToX)
        val absAngleToY = abs(angleToY)
        val absAngleToZ = abs(angleToZ)

        val upDir = when {
            absAngleToX > max(absAngleToY, absAngleToZ) -> {
                if (angleToX < 0) {
                    UpDirection.NEG_X_AXIS
                } else {
                    UpDirection.X_AXIS
                }
            }
            absAngleToY > max(absAngleToX, absAngleToZ) -> {
                if (angleToY < 0) {
                    UpDirection.NEG_Y_AXIS
                } else {
                    UpDirection.Y_AXIS
                }
            }
            else -> {
                if (angleToZ < 0) {
                    UpDirection.NEG_Z_AXIS
                } else {
                    UpDirection.Z_AXIS
                }
            }
        }
        log.info("normal: {}", normalDirection)
        log.info("angles x: $angleToX y: $angleToY z: $angleToZ")
        log.info("upDir: $upDir")
        return upDir
    }

    fun update(direction: Vector3f, mousePosition: Vector2f) {
        val tmp = Vector3f(direction).mul(movementSpeed)
        move(tmp)

        val mouseRot = Vector2f(mousePosition.x, mousePosition.y)
                .mul(mouseSensitivity)
        rotate(mouseRot)
        if (mouseRot.x != 0.0F || mouseRot.y != 0.0F) {
            log.info("MouseRot: {}, MousePosition: {}", mouseRot, mousePosition)
        }
    }
}
