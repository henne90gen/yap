package de.yap.game

import de.yap.engine.Mesh
import org.joml.Intersectionf
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

data class IntersectionResult(val point: Vector3f = Vector3f(), val distanceSquared: Float = Float.MAX_VALUE)

fun intersects(rayStart: Vector3f, direction: Vector3f, mesh: Mesh, transformation: Matrix4f): IntersectionResult {
    var closestIntersection = IntersectionResult()

    for (i in mesh.indices.indices step 3) {
        val index1 = mesh.indices[i] * 3
        val index2 = mesh.indices[i + 1] * 3
        val index3 = mesh.indices[i + 2] * 3
        // ATTENTION: assuming that vertices are always 3 floats wide
        val v1 = Vector4f(mesh.vertices[index1], mesh.vertices[index1 + 1], mesh.vertices[index1 + 2], 1.0F).mul(transformation)
        val v2 = Vector4f(mesh.vertices[index2], mesh.vertices[index2 + 1], mesh.vertices[index2 + 2], 1.0F).mul(transformation)
        val v3 = Vector4f(mesh.vertices[index3], mesh.vertices[index3 + 1], mesh.vertices[index3 + 2], 1.0F).mul(transformation)
        val vec1 = Vector3f(v1.x, v1.y, v1.z)
        val vec2 = Vector3f(v2.x, v2.y, v2.z)
        val vec3 = Vector3f(v3.x, v3.y, v3.z)

        val intersectionResult = intersects(rayStart, direction, vec1, vec2, vec3)
        if (intersectionResult.distanceSquared < closestIntersection.distanceSquared) {
            closestIntersection = intersectionResult
        }
    }

    return closestIntersection
}

fun intersects(rayStart: Vector3f, direction: Vector3f, vec1: Vector3f, vec2: Vector3f, vec3: Vector3f): IntersectionResult {
    val epsilon = 0.0000001F
    val t = Intersectionf.intersectRayTriangle(rayStart, direction, vec1, vec2, vec3, epsilon)
    if (t == -1.0F) {
        return IntersectionResult()
    }
    val dir = Vector3f(direction)
            .normalize()
            .mul(t)
    val point = Vector3f(rayStart).add(dir)
    return IntersectionResult(point, dir.lengthSquared())
}
