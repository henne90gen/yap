package de.yap.game

import de.yap.engine.ecs.entities.*
import org.joml.Vector3f
import kotlin.random.Random

class LevelGenerator() {

    fun generateLevelEntities(): MutableMap<Vector3f, Entity> {

        val width = Random.nextInt(15, 30)
        val depth = Random.nextInt(15, 30)
        val entities = mutableMapOf<Vector3f, Entity>()

        // ground floor
        for (x in 0..width) {
            for (y in 0..depth) {
                val position = Vector3f(x.toFloat(), -1F, y.toFloat())
                entities[position] = BlockEntity.singleTextureBlock(position, CHECKER_BOARD)

                val i = Random.nextInt(50)
                val randPos = Vector3f(x.toFloat(), 0F, y.toFloat())
                when (i) {
                    1 -> {
                        entities[randPos] = TableEntity(randPos)
                    }
                    2 -> {
                        entities[randPos] = ChairEntity(randPos)
                    }
                    3 -> {
                        entities[randPos] = WasteBinEntity(randPos)
                    }
                    4 -> {
                        entities[randPos] = ShoeShelfEntity(randPos)
                    }
                    5 -> {
                        entities[randPos] = WardrobeEntity(randPos)
                        if (Random.nextInt(2) == 1) {
                            val aboveRandPos = Vector3f(randPos.x, randPos.y + 1, randPos.z)
                            entities[aboveRandPos] = WardrobeEntity(aboveRandPos)
                        }
                    }
                }
            }
        }

        // side walls
        for (x in -1..width+1) {
            val position1 = Vector3f(x.toFloat(), 0F, -1F)
            entities[position1] = BlockEntity.singleTextureBlock(position1, RED)
            val position2 = Vector3f(x.toFloat(), 0F, depth.toFloat()+1)
            entities[position2] = BlockEntity.singleTextureBlock(position2, RED)

            val position3 = Vector3f(x.toFloat(), 1F, -1F)
            entities[position3] = BlockEntity.singleTextureBlock(position3, RED)
            val position4 = Vector3f(x.toFloat(), 1F, depth.toFloat()+1)
            entities[position4] = BlockEntity.singleTextureBlock(position4, RED)
        }
        for (z in 0..depth) {
            val position1 = Vector3f(-1F, 0F, z.toFloat())
            entities[position1] = BlockEntity.singleTextureBlock(position1, BLUE)
            val position2 = Vector3f(width.toFloat()+1, 0F, z.toFloat())
            entities[position2] = BlockEntity.singleTextureBlock(position2, GREEN)

            val position3 = Vector3f(-1F, 1F, z.toFloat())
            entities[position3] = BlockEntity.singleTextureBlock(position3, BLUE)
            val position4 = Vector3f(width.toFloat()+1, 1F, z.toFloat())
            entities[position4] = BlockEntity.singleTextureBlock(position4, GREEN)
        }

        return entities
    }

    private fun placeChairs(entities: Map<Vector3f, Entity>) {

    }
}
