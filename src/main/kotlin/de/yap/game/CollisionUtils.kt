package de.yap.game

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.util.X_AXIS
import de.yap.engine.util.Y_AXIS
import de.yap.engine.util.Z_AXIS
import org.joml.*

data class IntersectionResult(
        val point: Vector3f = Vector3f(),
        val distanceSquared: Float = Float.MAX_VALUE,
        val normal: Vector3f = Vector3f()
) {
    fun hasValue(): Boolean {
        return distanceSquared != Float.MAX_VALUE
    }
}

data class TransformedBoundingBox(val boundingBox: BoundingBoxComponent, val transformation: Matrix4f)

fun intersects(rayStart: Vector3f, direction: Vector3f, transformedBoundingBoxes: List<TransformedBoundingBox>): IntersectionResult {
    var closestIntersection = IntersectionResult()

    for (transformedBoundingBox in transformedBoundingBoxes) {
        val mesh = transformedBoundingBox.boundingBox
        val transformation = transformedBoundingBox.transformation

        val minV4 = Vector4f(mesh.min.x, mesh.min.y, mesh.min.z, 1.0F)
        val maxV4 = Vector4f(mesh.max.x, mesh.max.y, mesh.max.z, 1.0F)
        val newMinV4 = transformation.transform(minV4)
        val newMaxV4 = transformation.transform(maxV4)
        val min = Vector3f(newMinV4.x, newMinV4.y, newMinV4.z)
        val max = Vector3f(newMaxV4.x, newMaxV4.y, newMaxV4.z)
        val intersectionResult = intersects(rayStart, direction, min, max)
        if (intersectionResult.distanceSquared < closestIntersection.distanceSquared) {
            closestIntersection = intersectionResult
        }
    }

    return closestIntersection
}

private fun intersects(rayStart: Vector3f, direction: Vector3f, min: Vector3f, max: Vector3f): IntersectionResult {
    val result = Vector2f()
    val success = Intersectionf.intersectRayAab(rayStart, direction, min, max, result)
    if (!success) {
        return IntersectionResult()
    }

    val t = if (result.x > 0.0F && result.x <= result.y) {
        result.x
    } else if (result.y > 0.0F && result.y <= result.x) {
        result.y
    } else {
        -1.0F
    }

    if (t < 0.0F) {
        // negative t means we intersected behind rayStart, we don't want that
        return IntersectionResult()
    }

    val dir = Vector3f(direction)
            .normalize()
            .mul(t)
    val point = Vector3f(rayStart).add(dir)
    val normal = getNormal(min, max, point)
    return IntersectionResult(point, dir.lengthSquared(), normal)
}

private fun getNormal(min: Vector3f, max: Vector3f, point: Vector3f): Vector3f {
    return when {
        min.x == point.x -> {
            Vector3f(X_AXIS).mul(-1.0F)
        }
        max.x == point.x -> {
            X_AXIS
        }
        min.y == point.y -> {
            Vector3f(Y_AXIS).mul(-1.0F)
        }
        max.y == point.y -> {
            Y_AXIS
        }
        min.z == point.z -> {
            Vector3f(Z_AXIS).mul(-1.0F)
        }
        max.z == point.z -> {
            Z_AXIS
        }
        else -> {
            Vector3f()
        }
    }
}
