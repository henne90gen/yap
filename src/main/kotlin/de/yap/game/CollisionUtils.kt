package de.yap.game

import de.yap.engine.mesh.Mesh
import org.joml.Intersectionf
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

data class IntersectionResult(val point: Vector3f = Vector3f(), val distanceSquared: Float = Float.MAX_VALUE, val normal: Vector3f = Vector3f()) {
    fun hasValue(): Boolean {
        return distanceSquared != Float.MAX_VALUE
    }
}

fun intersects(rayStart: Vector3f, direction: Vector3f, meshes: List<Mesh>, transformation: Matrix4f): IntersectionResult {
    var closestIntersection = IntersectionResult()

    for (mesh in meshes) {
        for (i in mesh.indices.indices) {
            val triangle = mesh.indices[i]

            val origV1 = mesh.vertices[triangle.x]
            val origV2 = mesh.vertices[triangle.y]
            val origV3 = mesh.vertices[triangle.z]
            val v1 = Vector4f(origV1.x, origV1.y, origV1.z, 1.0F).mul(transformation)
            val v2 = Vector4f(origV2.x, origV2.y, origV2.z, 1.0F).mul(transformation)
            val v3 = Vector4f(origV3.x, origV3.y, origV3.z, 1.0F).mul(transformation)
            val vec1 = Vector3f(v1.x, v1.y, v1.z)
            val vec2 = Vector3f(v2.x, v2.y, v2.z)
            val vec3 = Vector3f(v3.x, v3.y, v3.z)

            val intersectionResult = intersects(rayStart, direction, vec1, vec2, vec3)
            if (intersectionResult.distanceSquared < closestIntersection.distanceSquared) {
                closestIntersection = intersectionResult
            }
        }
    }

    return closestIntersection
}

private fun intersects(rayStart: Vector3f, direction: Vector3f, vec1: Vector3f, vec2: Vector3f, vec3: Vector3f): IntersectionResult {
    val epsilon = 0.0000001F
    val t = Intersectionf.intersectRayTriangle(rayStart, direction, vec1, vec2, vec3, epsilon)
    if (t < 0.0F) {
        // negative t means we intersected behind rayStart, we don't want that
        return IntersectionResult()
    }

    val dir = Vector3f(direction)
            .normalize()
            .mul(t)
    val point = Vector3f(rayStart).add(dir)
    val edge1 = Vector3f(vec2).sub(vec1)
    val edge2 = Vector3f(vec3).sub(vec1)
    val normal = edge1.cross(edge2)
    return IntersectionResult(point, dir.lengthSquared(), normal)
}
