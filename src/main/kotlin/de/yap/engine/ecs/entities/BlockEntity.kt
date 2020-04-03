package de.yap.engine.ecs.entities

import de.yap.engine.ecs.*
import de.yap.engine.graphics.TextureCoords
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Vector2i
import org.joml.Vector3f

class BlockEntity(position: Vector3f, textureCoords: TextureCoords, mesh: Mesh) : Entity() {

    companion object {
        fun singleTextureBlock(position: Vector3f, textureCoords: TextureCoords): BlockEntity {
            val mesh = MeshUtils.unitCube(YapGame.getInstance().renderer.textureMapMaterial, textureCoords.texMin, textureCoords.texMax)
            return BlockEntity(position, textureCoords, mesh)
        }

        fun singleTextureBlock(position: Vector3f, textureIndex: Vector2i): BlockEntity {
            return singleTextureBlock(position, TextureCoords.fromIndex(textureIndex.x, textureIndex.y))
        }
    }

    init {
        addComponent(PositionComponent(position))
        addComponent(MeshComponent(mesh))
        addComponent(RotationComponent(0.0F, 0.0F))
        addComponent(BoundingBoxComponent.unitCube())
        addComponent(TextureAtlasIndexComponent(textureCoords))
    }
}
