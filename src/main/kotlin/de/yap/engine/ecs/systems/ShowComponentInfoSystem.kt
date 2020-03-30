package de.yap.engine.ecs.systems

import de.yap.engine.ecs.Component
import de.yap.engine.ecs.Entity
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.graphics.Text
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import java.lang.reflect.Field

class ShowComponentInfoSystem : ISystem(PositionComponent::class.java) {
    companion object {
        private val log: Logger = LogManager.getLogger()

        fun componentToText(component: Component): String {
            val clazz = component.javaClass
            var result = clazz.simpleName + "\n"
            for (field in clazz.fields) {
                val valueString = getValueString(field, component)
                result += "    " + field.name + ": " + valueString + "\n"
            }
            for (field in clazz.declaredFields) {
                val valueString = getValueString(field, component)
                result += "    " + field.name + ": " + valueString + "\n"
            }
            return result
        }

        private fun getValueString(field: Field, component: Component): String? {
            return try {
                if (!field.canAccess(component)) {
                    if (!field.trySetAccessible()) {
                        return null
                    }
                }

                field.get(component).toString()
            } catch (e: IllegalAccessException) {
                null
            }
        }
    }

    private var enabled = true

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }
        for (entity in entities) {
            renderComponentInfo(entity)
        }
    }

    private fun renderComponentInfo(entity: Entity) {
        // TODO only render this info on mouse hover
        // TODO add setting that lets you switch between mouse hover showing and always showing

        val positionComponent = entity.getComponent<PositionComponent>()
        val yapGame = YapGame.getInstance()
        val fontRenderer = yapGame.fontRenderer
        // TODO rotate the text to face the camera
        // TODO move the text outside the object it belongs to
        val transform = Matrix4f()
                .translate(positionComponent.position)
                .scale(0.5F)
        val componentsText = getComponentsText(entity)
        val text = Text(componentsText, fontRenderer.font, transform)
        fontRenderer.stringInScene(text, yapGame.view, yapGame.projection)
    }

    private fun getComponentsText(entity: Entity): String {
        return entity.components
                .map { it.value }
                .map(::componentToText)
                .reduce { acc, str -> acc + str }
    }
}
