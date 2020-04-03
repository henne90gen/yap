package de.yap.game

import de.yap.engine.ecs.entities.*
import de.yap.engine.graphics.*

import org.joml.Vector3f
import kotlin.math.PI
import kotlin.random.Random

class LevelGenerator() {

    private val width = Random.nextInt(15, 30)
    private val depth = Random.nextInt(15, 30)

    fun generateLevelEntities(): MutableCollection<Entity> {
        val entities = mutableMapOf<Vector3f, Entity>()

        addFloor(entities)
        addFurniture(entities)
        addWalls(entities)

        return entities.values
    }

    private fun addFloor(entities: MutableMap<Vector3f, Entity>) {
        for (x in 0..width) {
            for (y in 0..depth) {
                val position = Vector3f(x.toFloat(), -1F, y.toFloat())
                entities[position] = BlockEntity.singleTextureBlock(position, CHECKER_BOARD)
            }
        }
    }

    private fun addFurniture(entities: MutableMap<Vector3f, Entity>) {
        for (x in 0..width) {
            for (y in 0..depth) {

                val i = Random.nextInt(50)
                val randPos = Vector3f(x.toFloat(), 0F, y.toFloat())
                val randOrientation = Random.nextInt(4) * 0.5F * PI.toFloat()
                when (i) {
                    1 -> {
                        entities[randPos] = TableEntity(randPos)
                    }
                    2 -> {
                        entities[randPos] = ChairEntity(randPos, yaw = 0.0F, pitch = randOrientation )
                    }
                    3 -> {
                        entities[randPos] = WasteBinEntity(randPos)
                    }
                    4 -> {
                        entities[randPos] = ShoeShelfEntity(randPos, yaw = 0.0F, pitch = randOrientation)
                    }
                    5 -> {
                        entities[randPos] = WardrobeEntity(randPos)
                        if (Random.nextInt(2) == 1) {
                            val aboveRandPos = Vector3f(randPos.x, randPos.y + 1, randPos.z)
                            entities[aboveRandPos] = WardrobeEntity(aboveRandPos)
                        }
                    }
                    6 -> {
                        entities[randPos] = FridgeEntity(randPos)
                        if (Random.nextInt(2) == 1) {
                            val aboveRandPos = Vector3f(randPos.x, randPos.y + 1, randPos.z)
                            entities[aboveRandPos] = FridgeEntity(aboveRandPos)
                        }
                    }
                    7 -> {
                        entities[randPos] = OvenEntity(randPos, yaw = 0.0F, pitch = randOrientation)
                    }
                }
            }
        }
    }

    private fun addWalls(entities: MutableMap<Vector3f, Entity>) {
        fun genWindow(position: Vector3f, rotation: Float) {
            if (Random.nextInt(7) == 1) {
                entities[position] = WindowEntity(position, pitch = rotation)
            }
        }

        for (x in -1..width+1) {
            val wall1Layer1 = Vector3f(x.toFloat(), 0F, -1F)
            entities[wall1Layer1] = BlockEntity.singleTextureBlock(wall1Layer1, RED)
            val wall2Layer1 = Vector3f(x.toFloat(), 0F, depth.toFloat()+1)
            entities[wall2Layer1] = BlockEntity.singleTextureBlock(wall2Layer1, RED)

            val wall1Layer2 = Vector3f(x.toFloat(), 1F, -1F)
            entities[wall1Layer2] = BlockEntity.singleTextureBlock(wall1Layer2, RED)
            genWindow(wall1Layer2,  0F)
            val wall2Layer2 = Vector3f(x.toFloat(), 1F, depth.toFloat()+1)
            entities[wall2Layer2] = BlockEntity.singleTextureBlock(wall2Layer2, RED)
            genWindow(wall2Layer2,  0F)

            val wall1Layer3 = Vector3f(x.toFloat(), 2F, -1F)
            entities[wall1Layer3] = BlockEntity.singleTextureBlock(wall1Layer3, RED)
            val wall2Layer3 = Vector3f(x.toFloat(), 2F, depth.toFloat()+1)
            entities[wall2Layer3] = BlockEntity.singleTextureBlock(wall2Layer3, RED)
        }
        for (z in 0..depth) {
            val wall3Layer1 = Vector3f(-1F, 0F, z.toFloat())
            entities[wall3Layer1] = BlockEntity.singleTextureBlock(wall3Layer1, BLUE)
            val wall4Layer1 = Vector3f(width.toFloat()+1, 0F, z.toFloat())
            entities[wall4Layer1] = BlockEntity.singleTextureBlock(wall4Layer1, GREEN)

            val wall3Layer2 = Vector3f(-1F, 1F, z.toFloat())
            entities[wall3Layer2] = BlockEntity.singleTextureBlock(wall3Layer2, BLUE)
            genWindow(wall3Layer2,  0.5F * PI.toFloat())

            val wall4Layer2 = Vector3f(width.toFloat() + 1, 1F, z.toFloat())
            entities[wall4Layer2] = BlockEntity.singleTextureBlock(wall4Layer2, GREEN)
            genWindow(wall4Layer2,  0.5F * PI.toFloat())

            val wall3Layer3 = Vector3f(-1F, 2F, z.toFloat())
            entities[wall3Layer3] = BlockEntity.singleTextureBlock(wall3Layer3, BLUE)
            val wall4Layer3 = Vector3f(width.toFloat() + 1F, 2F, z.toFloat())
            entities[wall4Layer3] = BlockEntity.singleTextureBlock(wall4Layer3, GREEN)
        }
    }

    private fun placeChairs(entities: Map<Vector3f, Entity>) {

    }
}
