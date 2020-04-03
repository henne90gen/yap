package de.yap.engine.util

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.TextureAtlasIndexComponent
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.entities.PlayerEntity
import org.apache.logging.log4j.LogManager
import org.joml.Vector2i
import org.joml.Vector3f
import java.io.File

class LevelUtils {
    companion object {
        private const val LEVEL_FILE_VERSION = 1
        private val log = LogManager.getLogger()

        fun loadLevel(file: File?): List<Entity> {
            if (file == null) {
                log.info("Could not load level. (No file selected)")
                return emptyList()
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
                    'v' -> version = parseVersion(line)
                    'b' -> parseBlock(line, lineNumber, result)
                }

                if (version != LEVEL_FILE_VERSION) {
                    log.error("Could not load level. (Wrong version, expected version $LEVEL_FILE_VERSION but got version $version)")
                    return emptyList()
                }
            }

            log.info("Done.")
            return result
        }

        private fun parseBlock(line: String, lineNumber: Int, result: MutableList<Entity>) {
            try {

                val rest = line.substring(2)
                val parts = rest.split(" ")
                val position = Vector3f(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
                val tx = parts[3].toInt()
                val ty = parts[4].toInt()
                result.add(BlockEntity.singleTextureBlock(position, Vector2i(tx, ty)))
            } catch (e: NumberFormatException) {
                log.warn("Could not parse block entity on line $lineNumber.")
            }
        }

        private fun parseVersion(line: String): Int {
            return try {
                line.substring(2).toInt()
            } catch (e: NumberFormatException) {
                0
            }
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
                for (entity in entities) {
                    if (entity is PlayerEntity) {
                        continue
                    }
                    if (entity is BlockEntity) {
                        val positionComponent = entity.getComponent<PositionComponent>()
                        val x = positionComponent.position.x
                        val y = positionComponent.position.y
                        val z = positionComponent.position.z
                        val id = entity.getComponent<TextureAtlasIndexComponent>().textureCoords
                        val tx = id.x
                        val ty = id.y
                        it.write("b $x $y $z $tx $ty\n")
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
    }
}
