package de.yap.engine.ecs.entities

import de.yap.engine.ecs.BoundingBoxComponent
import de.yap.engine.ecs.MeshComponent
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

data class BlockTextureCoords(val texMin: Vector2f, val texMax: Vector2f) {
    companion object {
        fun fromIndex(x: Int, y: Int): BlockTextureCoords {
            // assumes using 32x32px Textures
            val texMin = Vector2f(1F/32F * x, 1F/32F * y)
            val texMax = Vector2f(1F/32F * (x+1), 1F/32F * (y+1))
            return BlockTextureCoords(texMin, texMax)
        }
    }
}

val CHECKER_BOARD = BlockTextureCoords.fromIndex(8, 0)

val RED = BlockTextureCoords.fromIndex(1, 2)
val BLUE = BlockTextureCoords(texMin = Vector2f(0.094F, 0.0313F), texMax = Vector2f(0.094F, 0.0313F))
val GREEN = BlockTextureCoords.fromIndex(6, 1)

class BlockEntity(position: Vector3f, mesh: Mesh) : Entity() {

    companion object {
        fun singleTextureBlock(position: Vector3f, textureCoords: Vector2i): BlockEntity {
            val blockTextureCoords = BlockTextureCoords.fromIndex(textureCoords.x, textureCoords.y)
            val mesh = MeshUtils.unitCube(YapGame.getInstance().renderer.textureMapMaterial, blockTextureCoords.texMin, blockTextureCoords.texMax)
            return BlockEntity(position, mesh)
        }
    }

    init {
        addComponent(PositionComponent(position))
        addComponent(MeshComponent(mesh))
        addComponent(RotationComponent(0.0F, 0.0F))
        addComponent(BoundingBoxComponent.unitCube())
    }
}
