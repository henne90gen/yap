package de.yap.engine.util

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.StaticEntityComponent
import de.yap.engine.ecs.TextureAtlasIndexComponent
import de.yap.engine.ecs.entities.*
import de.yap.engine.graphics.TextureCoords
import org.apache.logging.log4j.LogManager
import org.joml.Vector2f
import org.joml.Vector3f
import java.io.File
import java.io.OutputStreamWriter

class LevelUtils {
    companion object {
        private const val LEVEL_FILE_VERSION = 1
        private val log = LogManager.getLogger()

        fun loadLevel(file: File?, func: (List<Entity>) -> Unit) {
            if (file == null) {
                log.info("Could not load level. (No file selected)")
                return
            }

            log.info("Loading level $file...")
            val lines = file.readLines()

            var version = 0
            val result = mutableListOf<Entity>()
            for (lineWithIndex in lines.withIndex()) {
                val line = lineWithIndex.value
                if (line.isEmpty()) {
                    continue
                }

                val lineNumber = lineWithIndex.index + 1
                when (line[0]) {
                    'v' -> version = readVersion(line)
                    'b' -> readBlock(line, lineNumber, result)
                    's' -> readStaticEntity(line, lineNumber, result)
                }

                if (version != LEVEL_FILE_VERSION) {
                    log.error("Could not load level. (Wrong version, expected version $LEVEL_FILE_VERSION but got version $version)")
                    return
                }
            }

            func(result)

            log.info("Done.")
        }

        fun saveLevel(file: File?, entities: List<Entity>) {
            if (file == null) {
                log.info("Could not save level. (No file selected)")
                return
            }
            val levelFile = addLevelFileExtension(file)
            log.info("Saving level $levelFile...")

            levelFile.writer().use {
                it.write("v $LEVEL_FILE_VERSION\n")
                loop@ for (entity in entities) {
                    when (entity) {
                        is PlayerEntity -> continue@loop
                        is BlockEntity -> writeBlock(it, entity)
                        is StaticEntity -> writeStaticEntity(it, entity)
                    }
                }
            }
            log.info("Done.")
        }

        fun addLevelFileExtension(file: File): File {
            if (file.extension == "hse") {
                return file
            }
            val dir = file.parentFile
            val name = file.name + ".hse"
            return File(dir, name)
        }

        private fun readStaticEntity(line: String, lineNumber: Int, result: MutableList<Entity>) {
            try {
                val rest = line.substring(2)
                val parts = rest.split(" ")
                val type = getStaticEntityType(parts[0].toInt())
                val position = Vector3f(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat())
                val pitch = if (parts.size >= 5) {
                    parts[4].toFloat()
                } else {
                    0.0F
                }
                val yaw = if (parts.size >= 6) {
                    parts[5].toFloat()
                } else {
                    0.0F
                }
                val entity = StaticEntity(type, position, pitch, yaw)
                result.add(entity)
            } catch (e: NumberFormatException) {
                log.warn("Could not parse static entity on line $lineNumber.")
            }
        }

        private fun getStaticEntityType(i: Int): StaticEntityType {
            for (type in StaticEntityType.values()) {
                if (type.id == i) {
                    return type
                }
            }
            throw RuntimeException("ID $i does not correspond to a StaticEntity")
        }

        private fun readBlock(line: String, lineNumber: Int, result: MutableList<Entity>) {
            try {
                val rest = line.substring(2)
                val parts = rest.split(" ")
                val position = Vector3f(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
                val tMin = Vector2f(parts[3].toFloat(), parts[4].toFloat())
                val tMax = Vector2f(parts[5].toFloat(), parts[6].toFloat())
                result.add(BlockEntity.singleTextureBlock(position, TextureCoords(tMin, tMax)))
            } catch (e: NumberFormatException) {
                log.warn("Could not parse block entity on line $lineNumber.")
            }
        }

        private fun readVersion(line: String): Int {
            return try {
                line.substring(2).toInt()
            } catch (e: NumberFormatException) {
                0
            }
        }

        private fun writeStaticEntity(it: OutputStreamWriter, entity: StaticEntity) {
            val id = entity.getComponent<StaticEntityComponent>().id.ordinal
            val position = entity.getComponent<PositionComponent>().position
            val x = position.x
            val y = position.y
            val z = position.z
            val rotationComponent = entity.getComponent<RotationComponent>()
            val pitch = rotationComponent.pitch
            val yaw = rotationComponent.yaw
            it.write("s $id $x $y $z $pitch $yaw\n")
        }

        private fun writeBlock(it: OutputStreamWriter, entity: Entity) {
            val positionComponent = entity.getComponent<PositionComponent>()
            val x = positionComponent.position.x
            val y = positionComponent.position.y
            val z = positionComponent.position.z
            val id = entity.getComponent<TextureAtlasIndexComponent>().textureCoords
            val tMinX = id.texMin.x
            val tMinY = id.texMin.y
            val tMaxX = id.texMax.x
            val tMaxY = id.texMax.y
            it.write("b $x $y $z $tMinX $tMinY $tMaxX $tMaxY\n")
        }
    }
}
