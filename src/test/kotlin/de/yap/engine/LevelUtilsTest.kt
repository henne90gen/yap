package de.yap.engine

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.TextureAtlasIndexComponent
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.graphics.TextureCoords
import de.yap.engine.util.LevelUtils
import org.joml.Vector2f
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
        val entities = listOf<BlockEntity>(
                BlockEntity.singleTextureBlock(Vector3f(1.0F, 2.0F, 3.0F), TextureCoords(Vector2f(0.0F), Vector2f(0.5F))),
                BlockEntity.singleTextureBlock(Vector3f(4.0F, 5.0F, 6.0F), TextureCoords(Vector2f(0.5F), Vector2f(1.0F)))
        )
        LevelUtils.saveLevel(file, entities)

        val expectedLines = listOf("v 1", "b 1.0 2.0 3.0 0.0 0.0 0.5 0.5", "b 4.0 5.0 6.0 0.5 0.5 1.0 1.0")
        val lines = file.readLines()
        assertEquals(expectedLines, lines)
    }

    @Test
    fun testLoadLevel() {
        val lines = listOf("v 1", "b 1.0 2.0 3.0 0.0 0.0 0.5 0.5", "b 4.0 5.0 6.0 0.5 0.5 1.0 1.0")
        val file = tempFolder.newFile("level.hse")
        file.writeText(lines.joinToString("\n"))

        val expectedEntities = listOf(
                BlockEntity.singleTextureBlock(Vector3f(1.0F, 2.0F, 3.0F), TextureCoords(Vector2f(0.0F), Vector2f(0.5F))),
                BlockEntity.singleTextureBlock(Vector3f(4.0F, 5.0F, 6.0F), TextureCoords(Vector2f(0.5F), Vector2f(1.0F)))
        )
        val entities = LevelUtils.loadLevel(file)
        assertEquals(expectedEntities.size, entities.size)
        for (entity in entities.withIndex()) {
            val textureCoords = entity.value.getComponent<TextureAtlasIndexComponent>().textureCoords
            val expectedTextureCoords = expectedEntities[entity.index].getComponent<TextureAtlasIndexComponent>().textureCoords
            assertEquals(expectedTextureCoords, textureCoords)

            val blockPosition = entity.value.getComponent<PositionComponent>().position
            val expectedBlockPosition = expectedEntities[entity.index].getComponent<PositionComponent>().position
            assertEquals(expectedBlockPosition, blockPosition)
        }
    }
}
