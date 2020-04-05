package de.yap.game

import de.yap.engine.ecs.entities.*
import de.yap.engine.graphics.*

import org.joml.Vector3f
import kotlin.math.PI
import kotlin.random.Random

class LevelGenerator() {

    private val width = Random.nextInt(20, 50)
    private val depth = Random.nextInt(20, 50)

    fun generateLevelEntities(): MutableCollection<Entity> {
        val entities = mutableMapOf<Vector3f, Entity>()

        addFloor(entities)
        addWalls(entities)
        addFurniture(entities)

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
                        entities[randPos] = StaticEntity(StaticEntities.TABLE, randPos)
                    }
                    2 -> {
                        entities[randPos] = StaticEntity(StaticEntities.CHAIR, randPos, yaw = randOrientation)
                    }
                    3 -> {
                        entities[randPos] = StaticEntity(StaticEntities.WASTE_BIN, randPos)
                    }
                    4 -> {
                        entities[randPos] = StaticEntity(StaticEntities.SHOE_SHELF, randPos, yaw = randOrientation)
                    }
                    5 -> {
                        entities[randPos] = StaticEntity(StaticEntities.WARDROBE, randPos)
                        if (Random.nextInt(2) == 1) {
                            val aboveRandPos = Vector3f(randPos.x, randPos.y + 1, randPos.z)
                            entities[aboveRandPos] = StaticEntity(StaticEntities.WARDROBE, aboveRandPos)
                        }
                    }
                    6 -> {
                        entities[randPos] = StaticEntity(StaticEntities.FRIDGE, randPos)
                        if (Random.nextInt(2) == 1) {
                            val aboveRandPos = Vector3f(randPos.x, randPos.y + 1, randPos.z)
                            entities[aboveRandPos] = StaticEntity(StaticEntities.FRIDGE, aboveRandPos)
                        }
                    }
                    7 -> {
                        entities[randPos] = StaticEntity(StaticEntities.OVEN, randPos, yaw = randOrientation)
                    }
                    8 -> {
                        entities[randPos] = StaticEntity(StaticEntities.KITCHEN_CABINET, randPos, yaw = randOrientation)
                    }
                }
            }
        }
    }

    private fun addWalls(entities: MutableMap<Vector3f, Entity>) {
        fun genWindow(position: Vector3f, rotation: Float) {
            if (Random.nextInt(7) == 1) {
                entities[position] = StaticEntity(StaticEntities.WINDOW, position, yaw = rotation)
            }
        }

        val pos = Vector3f(3F, 2F, 0F)
        entities[pos] = StaticEntity(StaticEntities.CLOCK, pos)

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
