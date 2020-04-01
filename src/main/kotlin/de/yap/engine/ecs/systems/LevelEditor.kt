package de.yap.engine.ecs.systems

import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.entities.Entity
import de.yap.game.IntersectionResult
import de.yap.game.TransformedMesh
import de.yap.game.YapGame
import de.yap.game.intersects
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class LevelEditor : ISystem(MeshComponent::class.java, PositionComponent::class.java) {

    private var intersectionResult: IntersectionResult = IntersectionResult()

    override fun render(entities: List<Entity>) {
        renderCrosshair()
        renderSelectedBlock()
        renderCurrentPosition()
    }

    private fun renderCurrentPosition() {
        val cameraEntity = FirstPersonCamera.currentCameraEntity
        cameraEntity?.let {
            val positionComponent = it.getComponent<PositionComponent>()
            val transform = Matrix4f()
                    .translate(-0.3F, 0.8F, 0.0F)
                    .scale(0.3F)
            YapGame.getInstance().fontRenderer.string("${positionComponent.position}", transform)
        }
    }

    private fun renderSelectedBlock() {
        if (!intersectionResult.hasValue()) {
            return
        }

        val renderer = YapGame.getInstance().renderer
        val clampedPoint = clampPoint(intersectionResult)
        renderer.wireframe {
            val transformation = Matrix4f()
                    .translate(clampedPoint)
            val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
            renderer.cube(transformation, color)
        }

        var transform = Matrix4f()
                .translate(-0.3F, 0.7F, 0.0F)
                .scale(0.3F)
        YapGame.getInstance().fontRenderer.string("${intersectionResult.normal}", transform)

        transform = Matrix4f()
                .translate(-0.3F, 0.6F, 0.0F)
                .scale(0.3F)
        YapGame.getInstance().fontRenderer.string("$clampedPoint", transform)
    }

    private fun clampPoint(intersectionResult: IntersectionResult): Vector3f {
        val normal = intersectionResult.normal
        val point = Vector3f(intersectionResult.point).add(Vector3f(normal).absolute().mul(0.01F))

        var x = point.x
        x = clamp(x)
        x += if (normal.x == 0.0F) {
            0.5F
        } else {
            0.5F * normal.x
        }

        var y = point.y
        y = clamp(y)
        y += if (normal.y == 0.0F) {
            0.5F
        } else {
            0.5F * normal.y
        }

        var z = point.z
        z = clamp(z)
        z += if (normal.z == 0.0F) {
            0.5F
        } else {
            0.5F * normal.z
        }

        return Vector3f(x, y, z)
    }

    private fun clamp(num: Float): Float {
        return if (num < 0.0F) {
            (num - 1.0F).toInt().toFloat()
        } else {
            num.toInt().toFloat()
        }
    }

    private fun renderCrosshair() {
        val renderer = YapGame.getInstance().renderer
        renderer.inScreenSpace {
            val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)

            val transformationHorizontal = Matrix4f()
                    .scale(0.1F, 0.005F, 1.0F)
            renderer.quad(transformationHorizontal, color)

            val transformationVertical = Matrix4f()
                    .scale(0.005F, 0.1F, 1.0F)
            renderer.quad(transformationVertical, color)
        }
    }

    override fun update(interval: Float, entities: List<Entity>) {
        val meshes = entities.map {
            TransformedMesh(
                    it.getComponent<MeshComponent>().mesh,
                    Matrix4f().translate(it.getComponent<PositionComponent>().position)
            )
        }
        val cameraEntity = FirstPersonCamera.currentCameraEntity
        var rayStart = Vector3f(0.0F)
        var direction = Vector3f(0.0F, 0.0F, -1.0F)
        cameraEntity?.let {
            rayStart = it.getComponent<PositionComponent>().position
            direction = it.getComponent<RotationComponent>().direction()
        }
        intersectionResult = intersects(rayStart, direction, meshes)
    }
}
