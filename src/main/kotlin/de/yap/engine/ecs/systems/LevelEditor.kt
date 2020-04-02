package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.game.IntersectionResult
import de.yap.game.TransformedMesh
import de.yap.game.YapGame
import de.yap.game.intersects
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.awt.event.WindowEvent
import javax.swing.*


class LevelEditor : ISystem(MeshComponent::class.java, PositionComponent::class.java) {

    private var reactToMouseInput = true
    private var clampedPoint: Vector3f? = null
    private var normal: Vector3f? = null
    private var selectedBlockId = 0

    private val frame = JFrame("Settings")

    override fun init() {
        val saveLoadButtons = createSaveLoadButtons()
        frame.add(saveLoadButtons)

        val blockSettings = createBlockSettings()
        frame.add(blockSettings)

        frame.add(Box.createVerticalGlue())

        frame.setSize(300, 500)
        frame.focusableWindowState = false
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)
        frame.isVisible = true
    }

    private fun createBlockSettings(): JPanel {
        val blockSettings = JPanel()
        blockSettings.layout = BoxLayout(blockSettings, BoxLayout.X_AXIS)

        // TODO this has been copied from BlockEntity.AVAILABLE_BLOCKS
        val items = arrayOf("Grass", "Sand", "Wood", "Rock")
        val materialCombo = JComboBox(items)
        materialCombo.addActionListener {
            selectedBlockId = when (materialCombo.selectedItem) {
                "Grass" -> 0
                "Sand" -> 1
                "Wood" -> 2
                "Rock" -> 3
                else -> selectedBlockId
            }
        }
        blockSettings.add(materialCombo)
        return blockSettings
    }

    private fun createSaveLoadButtons(): JPanel {
        val saveLoadButtons = JPanel()
        saveLoadButtons.layout = BoxLayout(saveLoadButtons, BoxLayout.X_AXIS)

        val loadLevelBtn = JButton("Load Level")
        loadLevelBtn.addActionListener { println("Loading level") }
        saveLoadButtons.add(loadLevelBtn)
        val saveLevelBtn = JButton("Save Level")
        saveLevelBtn.addActionListener { println("Saving level") }
        saveLoadButtons.add(saveLevelBtn)
        return saveLoadButtons
    }

    @Subscribe
    fun onWindowClose(event: WindowCloseEvent) {
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }

    @Subscribe
    fun onMouseClick(event: MouseClickEvent) {
        if (!reactToMouseInput) {
            return
        }

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_1 && event.action == GLFW.GLFW_RELEASE) {
            removeBlock()
        } else if (event.button == GLFW.GLFW_MOUSE_BUTTON_2 && event.action == GLFW.GLFW_RELEASE) {
            placeBlock()
        }
    }

    @Subscribe
    fun onKeyboardPress(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_LEFT_ALT && event.action == GLFW.GLFW_RELEASE) {
            toggleReactToMouseInput()
        }
    }

    private fun toggleReactToMouseInput() {
        val game = YapGame.getInstance()
        game.window.toggleMouseVisibility()
        game.firstPersonCamera.toggleMouseMovementTracking()
        reactToMouseInput = !reactToMouseInput
    }

    private fun removeBlock() {
        clampedPoint?.let { p ->
            normal?.let { n ->
                val game = YapGame.getInstance()
                val removalPoint = Vector3f(p).sub(n)
                // TODO use a spacial query
                val entities = game.entityManager.getEntities(capability)
                for (entity in entities) {
                    val position = entity.getComponent<PositionComponent>().position
                    if (removalPoint == position) {
                        game.entityManager.removeEntity(entity)
                        break
                    }
                }
            }
        }
    }

    private fun placeBlock() {
        clampedPoint?.let {
            val entity = BlockEntity(it, selectedBlockId)
            YapGame.getInstance().entityManager.addEntity(entity)
        }
    }

    override fun render(entities: List<Entity>) {
        renderCrosshair()
        renderSelectedBlock()
    }

    private fun renderSelectedBlock() {
        clampedPoint?.let {
            val renderer = YapGame.getInstance().renderer
            renderer.wireframe {
                val transformation = Matrix4f()
                        .translate(it)
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.cube(transformation, color)
            }
        }
    }

    private fun renderCrosshair() {
        val renderer = YapGame.getInstance().renderer
        renderer.inScreenSpace {
            val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)

            val transformationHorizontal = Matrix4f()
                    .scale(0.1F, 0.005F, 1.0F)
            renderer.quad(transformationHorizontal, color)

            val transformationVertical = Matrix4f()
                    .scale(0.005F, 0.1F, 1.0F)
            renderer.quad(transformationVertical, color)
        }
    }

    override fun update(interval: Float, entities: List<Entity>) {
        val meshes = entities.map {
            TransformedMesh(
                    it.getComponent<MeshComponent>().mesh,
                    Matrix4f().translate(it.getComponent<PositionComponent>().position)
            )
        }
        val cameraEntity = YapGame.getInstance().firstPersonCamera.getCurrentCamera()
        var rayStart = Vector3f(0.0F)
        var direction = Vector3f(0.0F, 0.0F, -1.0F)
        cameraEntity?.let {
            rayStart = it.getComponent<PositionComponent>().position
            direction = it.getComponent<RotationComponent>().direction()
        }
        val intersectionResult = intersects(rayStart, direction, meshes)
        if (intersectionResult.hasValue()) {
            clampedPoint = clampPoint(intersectionResult)
            normal = intersectionResult.normal
        } else {
            clampedPoint = null
            normal = null
        }
    }

    private fun clampPoint(intersectionResult: IntersectionResult): Vector3f? {
        if (!intersectionResult.hasValue()) {
            return null
        }

        val normal = intersectionResult.normal
        val point = Vector3f(intersectionResult.point).add(Vector3f(normal).absolute().mul(0.01F))

        var x = point.x
        x = clamp(x)
        x += if (normal.x == 0.0F) {
            0.5F
        } else {
            0.5F * normal.x
        }

        var y = point.y
        y = clamp(y)
        y += if (normal.y == 0.0F) {
            0.5F
        } else {
            0.5F * normal.y
        }

        var z = point.z
        z = clamp(z)
        z += if (normal.z == 0.0F) {
            0.5F
        } else {
            0.5F * normal.z
        }

        return Vector3f(x, y, z)
    }

    private fun clamp(num: Float): Float {
        return if (num < 0.0F) {
            (num - 1.0F).toInt().toFloat()
        } else {
            num.toInt().toFloat()
        }
    }
}
