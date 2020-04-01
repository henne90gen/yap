package de.yap.game

import de.yap.engine.ecs.entities.*
import org.joml.Vector3f
import kotlin.random.Random

class LevelGenerator() {

    fun generateLevelEntities(): MutableList<Entity> {

        val width = Random.nextInt(15, 30)
        val depth = Random.nextInt(15, 30)
        val entities: MutableList<Entity> = mutableListOf()

        // ground floor
        for (x in 0..width) {
            for (y in 0..depth) {
                val position = Vector3f(x.toFloat(), -1F, y.toFloat())
                entities.add(BlockEntity.singleTextureBlock(position, ROCK))

                val i = Random.nextInt(50)
                val randPos = Vector3f(x.toFloat(), 0F, y.toFloat())
                if (i == 1) {
                    entities.add(TableEntity(randPos))
                } else if (i == 2) {
                    entities.add(ChairEntity(randPos))
                }
            }
        }

        // side walls
        for (x in -1..width+1) {
            var position = Vector3f(x.toFloat(), 0F, -1F)
            entities.add(BlockEntity.singleTextureBlock(position, WOOD))
            position = Vector3f(x.toFloat(), 0F, depth.toFloat()+1)
            entities.add(BlockEntity.singleTextureBlock(position, WOOD))
        }
        for (z in 0..depth) {
            var position = Vector3f(-1F, 0F, z.toFloat())
            entities.add(BlockEntity.singleTextureBlock(position, WOOD))
            position = Vector3f(width.toFloat()+1, 0F, z.toFloat())
            entities.add(BlockEntity.singleTextureBlock(position, WOOD))
        }

        return entities
    }
}
