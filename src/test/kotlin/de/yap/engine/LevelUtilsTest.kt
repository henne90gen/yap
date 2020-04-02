package de.yap.engine

import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.util.LevelUtils
import org.joml.Vector3f
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals


class LevelUtilsTest {

    @get:Rule
    var tempFolder = TemporaryFolder()

    @Test
    fun testAddLevelFileExtensionWithCorrectExtension() {
        val file = File("/path/to/file.hse")
        val levelFile = LevelUtils.addLevelFileExtension(file)
        assertEquals("/path/to/file.hse", levelFile.absolutePath)
    }

    @Test
    fun testAddLevelFileExtensionWithMissingExtension() {
        val file = File("/path/to/file")
        val levelFile = LevelUtils.addLevelFileExtension(file)
        assertEquals("/path/to/file.hse", levelFile.absolutePath)
    }

    @Test
    fun testAddLevelFileExtensionWithIncorrectExtension() {
        val file = File("/path/to/file.txt")
        val levelFile = LevelUtils.addLevelFileExtension(file)
        assertEquals("/path/to/file.txt.hse", levelFile.absolutePath)
    }

    @Test
    fun testSaveLevel() {
        val file = tempFolder.newFile("level.hse")
        val entities = listOf(
                BlockEntity(Vector3f(0.0F, 0.0F, 0.0F), 0),
                BlockEntity(Vector3f(1.0F, 0.0F, 0.0F), 1)
        )
        LevelUtils.saveLevel(file, entities)

        TODO("read file content")
        TODO("validate file content")
    }

    @Test
    fun testLoadLevel() {

    }
}
