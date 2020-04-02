package de.yap.engine.util

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.LevelEditor
import java.io.File

class LevelUtils {
    companion object {
        fun loadLevel(file: File?): List<Entity> {
            // TODO move this into a separate class
            if (file == null) {
                LevelEditor.log.info("Could not load level. (No file selected)")
                return emptyList()
            }
            LevelEditor.log.info("Loading level $file...")
            LevelEditor.log.info("Done.")

            return emptyList()
        }

        fun saveLevel(file: File?, entities: List<Entity>) {
            // TODO move this into a separate class
            if (file == null) {
                LevelEditor.log.info("Could not save level. (No file selected)")
                return
            }
            val levelFile = addLevelFileExtension(file)
            LevelEditor.log.info("Saving level $levelFile...")
            for (entity in entities) {

            }
            LevelEditor.log.info("Done.")
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
