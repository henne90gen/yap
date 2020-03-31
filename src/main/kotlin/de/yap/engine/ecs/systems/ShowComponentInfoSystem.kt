package de.yap.engine.ecs.systems

import de.yap.engine.ecs.Component
import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.graphics.Text
import de.yap.engine.util.Y_AXIS
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import java.lang.reflect.Field

class ShowComponentInfoSystem : ISystem(PositionComponent::class.java) {
    companion object {
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

                val result = field.get(component).toString()
                val maxNumberOfCharacters = 40
                if (result.length > maxNumberOfCharacters) {
                    result.substring(0, maxNumberOfCharacters)
                } else {
                    result
                }
            } catch (e: IllegalAccessException) {
                null
            }
        }
    }

    // TODO enable again, when all TODOs in the renderComponentInfo are done
    private var enabled = false

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        entities.forEach(this::renderComponentInfo)
    }

    private fun renderComponentInfo(entity: Entity) {
        // TODO only render this info on mouse hover
        // TODO render this information as part of the HUD instead of in the world

        if (entity == FirstPersonCameraSystem.currentCameraEntity) {
            return
        }

        var lookAt = Vector3f(0.0F)
        FirstPersonCameraSystem.currentCameraEntity?.let {
            val positionComponent = it.getComponent<PositionComponent>()
            lookAt = positionComponent.position
        }

        val positionComponent = entity.getComponent<PositionComponent>()
        val yapGame = YapGame.getInstance()
        val fontRenderer = yapGame.fontRenderer

        val lookDirection = Vector3f(lookAt)
                .sub(positionComponent.position)
                .normalize()
        val projectedLookDir = Vector2f(lookDirection.x, lookDirection.z)
        val angle = -Vector2f(0.0F, 1.0F).angle(projectedLookDir)

        val transform = Matrix4f()
                .translate(positionComponent.position)
                .rotate(angle, Y_AXIS)
                .translate(0.3F, 0.0F, 0.0F)
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
