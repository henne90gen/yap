package de.yap.engine

import de.yap.engine.ecs.LevelEditorSettings
import de.yap.engine.ecs.entities.DynamicEntity
import de.yap.engine.ecs.entities.DynamicEntityType
import org.joml.Vector3f

fun main() {
    val settings = LevelEditorSettings()
    settings.init()
    val entity = DynamicEntity(DynamicEntityType.SIMPLE_AI, Vector3f(1.0F, 2.0F, 3.0F))
    settings.updateSelectedEntity(entity)
}
