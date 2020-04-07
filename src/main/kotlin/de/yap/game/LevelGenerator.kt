package de.yap.game

import de.yap.engine.ecs.entities.*
import de.yap.engine.graphics.*

import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random

class LevelGenerator() {

    private val width = Random.nextInt(20, 50)
    private val depth = Random.nextInt(20, 50)

    enum class RoomTypes() {
        KITCHEN,
        BATHROOM,
        BEDROOM,
        LIVINGROOM
    }

    fun generateLevelEntities(): MutableCollection<Entity> {
        val entities = mutableMapOf<Vector3f, Entity>()

        /* ToDo: more realistic level generation
            1) create side walls
            2) create walls for rooms
            3) insert doors between rooms
            4) give each room a type (e.g. kitchen, bath, bedroom, living room)
            5) furnish rooms according to room type
        */

        entities.putAll(genFloor())
        entities.putAll(genWalls())

        //addFurniture(entities)

        return entities.values
    }

    private fun genFloor(): MutableMap<Vector3f, Entity> {
        val floor = mutableMapOf<Vector3f, Entity>()
        for (x in 0..width) {
            for (y in 0..depth) {
                val position = Vector3f(x.toFloat(), -1F, y.toFloat())
                floor[position] = BlockEntity.singleTextureBlock(position, PLANKS)
            }
        }
        return floor
    }

    private fun genWalls(): MutableMap<Vector3f, Entity> {
        var walls = mutableMapOf<Vector3f, Entity>()
        var roomRatio = 1000
        while (roomRatio > 120) {
            walls = mutableMapOf()
            walls.putAll(genOutsideWalls())
            walls.putAll(genInsideWalls(walls))

            val roomAssignments = assignRooms(walls)
            val levelSize = width * depth
            roomRatio = levelSize / roomAssignments.size
        }
        return walls
    }

    data class Head(
            val pos: Vector3f, val direction: Vector3f,
            val turnProbability: Float, val splitProbability: Float,
            val movesSinceLastTurnOrSplit: Int
    )

    private fun genInsideWalls(entities: MutableMap<Vector3f, Entity>): MutableMap<Vector3f, Entity> {
        /** uses a snake like approach
         * -> snake has a hat and goes into one direction, over time probability to turn or split the head increases
         * -> collision with one self or a wall eliminates the snake head
         */
        val walls = mutableMapOf<Vector3f, Entity>()
        var heads = mutableListOf<Head>()
        val initialHead = Head(Vector3f(round(width / 2F), 0F, 0F), Vector3f(0F, 0F, 1F), 0F, 0F, 0)
        heads.add(initialHead)
        while (heads.size > 0) {
            val newHeads = mutableListOf<Head>()
            for (head in heads) {
                if (head.pos in walls || head.pos in entities) {
                    continue
                }

                walls[Vector3f(head.pos)] = BlockEntity.singleTextureBlock(Vector3f(head.pos), GREEN)
                if (Random.nextFloat() < head.turnProbability
                        && head.movesSinceLastTurnOrSplit > 5
                        && distanceToOuterWall(head.pos) > 3
                        && distanceToNextInnerWall(head.pos, head.direction, walls) > 1) {
                    // turn
                    if (Random.nextFloat() < head.splitProbability) {
                        // ... and split head
                        val direction1 = newRandomDirection(head.direction)
                        val pos1 = Vector3f(head.pos).add(direction1)
                        newHeads.add(Head(pos1, direction1, 0F, 0F, 0))

                        val direction2 = Vector3f(direction1).mul(Vector3f(-1F))
                        val pos2 = Vector3f(head.pos).add(direction2)
                        newHeads.add(Head(pos2, direction2, 0F, 0F, 0))
                    } else {
                        // only turn
                        val direction = newRandomDirection(head.direction)
                        val pos = Vector3f(head.pos).add(direction)
                        newHeads.add(Head(pos, direction, 0.05F, head.splitProbability, 0))
                    }
                } else {
                    // continue in same direction
                    val pos = Vector3f(head.pos).add(head.direction)

                    if (head.movesSinceLastTurnOrSplit > 5) {
                        newHeads.add(Head(pos, head.direction, head.turnProbability + 0.15F,
                                head.splitProbability + 0.15F, head.movesSinceLastTurnOrSplit + 1))
                    } else {
                        newHeads.add(Head(pos, head.direction, head.turnProbability , head.splitProbability,
                                head.movesSinceLastTurnOrSplit + 1))
                    }

                }
            }
            heads = newHeads
        }
        return walls
    }

    private fun distanceToOuterWall(pos: Vector3f): Float {
        val distanceX = min(pos.x + 1, width - pos.x)
        val distanceZ = min(pos.z + 1, depth - pos.z)
        return min(distanceX, distanceZ)
    }

    private fun distanceToNextInnerWall(pos: Vector3f, direction: Vector3f, walls: Map<Vector3f, Entity>): Int {
        var distance = 0
        var next = Vector3f(pos).add(direction)
        while (next !in walls && distanceToOuterWall(next) > 1) {
            distance += 1
            next = next.add(direction)
        }
        return distance
    }

    private fun newRandomDirection(oldDirection: Vector3f): Vector3f {
        val direction = Vector3f(0F, 0F, 0F)
        val newDirectionSign = if (Random.nextInt(2) == 1) 1F else -1F
        when {
            oldDirection.x == 0F -> {
                direction.x = newDirectionSign
            }
            oldDirection.z == 0F -> {
                direction.z = newDirectionSign
            }
            else -> {
                check(false) { "unexpected old direction $oldDirection" }
            }
        }
        return direction
    }

    private fun assignRooms(entities: MutableMap<Vector3f, Entity>, baseLevel: Float = 0F): List<Set<Vector3f>> {
        /**
         * return a list of sets where each set contains all positions on the given y-baseLevel that
         * correspond to the same room
         */
        val rooms = mutableListOf<Set<Vector3f>>()
        for (x in 0 until width) {
            for (y in 0 until depth) {
                val pos = Vector3f(x.toFloat(), baseLevel, y.toFloat())
                var roomAssigned = false
                for (room in rooms) {
                    if (pos in room) {
                        roomAssigned = true
                        break
                    }
                }
                if (!roomAssigned) {
                    val positions = horizontalFloodFill(pos, entities)
                    if (positions.isNotEmpty()) {
                        rooms.add(positions)
                    }
                }
            }
        }
        return rooms
    }

    private fun horizontalFloodFill(startPosition: Vector3f, entities: MutableMap<Vector3f, Entity>): Set<Vector3f> {
        val positions = mutableSetOf<Vector3f>()
        var remaining = mutableListOf(startPosition)
        while (remaining.size > 0) {
            val newRemaining = mutableListOf<Vector3f>()
            for (pos in remaining) {
                if (pos !in entities && pos !in positions) {
                    positions.add(Vector3f(pos))
                    newRemaining.add(Vector3f(pos.x + 1, pos.y, pos.z))
                    newRemaining.add(Vector3f(pos.x - 1, pos.y, pos.z))
                    newRemaining.add(Vector3f(pos.x, pos.y, pos.z + 1))
                    newRemaining.add(Vector3f(pos.x, pos.y, pos.z - 1))
                }
            }
            remaining = newRemaining
        }
        return positions.toSet()
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

    private fun genOutsideWalls(): MutableMap<Vector3f, Entity> {
        // ToDo refactor to something more elegant
        val walls = mutableMapOf<Vector3f, Entity>()
        fun genWindow(position: Vector3f, rotation: Float) {
            if (Random.nextInt(7) == 1) {
                walls[position] = StaticEntity(StaticEntities.WINDOW, position, yaw = rotation)
            }
        }

        val pos = Vector3f(3F, 2F, 0F)
        walls[pos] = StaticEntity(StaticEntities.CLOCK, pos)

        for (x in -1..width + 1) {
            val wall1Layer1 = Vector3f(x.toFloat(), 0F, -1F)
            walls[wall1Layer1] = BlockEntity.singleTextureBlock(wall1Layer1, RED)
            val wall2Layer1 = Vector3f(x.toFloat(), 0F, depth.toFloat() + 1)
            walls[wall2Layer1] = BlockEntity.singleTextureBlock(wall2Layer1, RED)

            val wall1Layer2 = Vector3f(x.toFloat(), 1F, -1F)
            walls[wall1Layer2] = BlockEntity.singleTextureBlock(wall1Layer2, RED)
            genWindow(wall1Layer2, 0F)
            val wall2Layer2 = Vector3f(x.toFloat(), 1F, depth.toFloat() + 1)
            walls[wall2Layer2] = BlockEntity.singleTextureBlock(wall2Layer2, RED)
            genWindow(wall2Layer2, 0F)

            val wall1Layer3 = Vector3f(x.toFloat(), 2F, -1F)
            walls[wall1Layer3] = BlockEntity.singleTextureBlock(wall1Layer3, RED)
            val wall2Layer3 = Vector3f(x.toFloat(), 2F, depth.toFloat() + 1)
            walls[wall2Layer3] = BlockEntity.singleTextureBlock(wall2Layer3, RED)
        }
        for (z in 0..depth) {
            val wall3Layer1 = Vector3f(-1F, 0F, z.toFloat())
            walls[wall3Layer1] = BlockEntity.singleTextureBlock(wall3Layer1, BLUE)
            val wall4Layer1 = Vector3f(width.toFloat() + 1, 0F, z.toFloat())
            walls[wall4Layer1] = BlockEntity.singleTextureBlock(wall4Layer1, GREEN)

            val wall3Layer2 = Vector3f(-1F, 1F, z.toFloat())
            walls[wall3Layer2] = BlockEntity.singleTextureBlock(wall3Layer2, BLUE)
            genWindow(wall3Layer2, 0.5F * PI.toFloat())

            val wall4Layer2 = Vector3f(width.toFloat() + 1, 1F, z.toFloat())
            walls[wall4Layer2] = BlockEntity.singleTextureBlock(wall4Layer2, GREEN)
            genWindow(wall4Layer2, 0.5F * PI.toFloat())

            val wall3Layer3 = Vector3f(-1F, 2F, z.toFloat())
            walls[wall3Layer3] = BlockEntity.singleTextureBlock(wall3Layer3, BLUE)
            val wall4Layer3 = Vector3f(width.toFloat() + 1F, 2F, z.toFloat())
            walls[wall4Layer3] = BlockEntity.singleTextureBlock(wall4Layer3, GREEN)
        }
        return walls
    }
}
