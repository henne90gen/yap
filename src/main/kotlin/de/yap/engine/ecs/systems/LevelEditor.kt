package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.entities.StaticEntities
import de.yap.engine.ecs.entities.StaticEntity
import de.yap.engine.util.LevelUtils
import de.yap.game.IntersectionResult
import de.yap.game.TransformedBoundingBox
import de.yap.game.YapGame
import de.yap.game.intersects
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileFilter


class LevelFileFilter : FileFilter() {
    override fun accept(file: File?): Boolean {
        if (file == null) {
            return false
        }
        return file.isDirectory || file.extension == "hse"
    }

    override fun getDescription(): String {
        return "Level Files (*.hse)"
    }
}

class ComboItem(val id: Int) {
    override fun toString(): String {
        return if (id == -1) {
            "Simple Block"
        } else {
            StaticEntities.values()[id].toString()
        }
    }
}

class LevelEditor : ISystem(BoundingBoxComponent::class.java, PositionComponent::class.java) {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private lateinit var frame: JFrame

    private var reactToMouseInput = true
    private var clampedPoint: Vector3f? = null
    private var normal: Vector3f? = null
    private var selectedTextureIndex = Vector2i(0)
    private var rotation = Vector3f(0.0F)
    private var entityTypeCombo: JComboBox<ComboItem>? = null

    override fun init() {
        frame = JFrame("Settings")
        frame.layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.insets = Insets(10, 10, 10, 10)

        addSaveLoadButtons(frame, constraints)
        addEntitySettings(frame, constraints)

        // add more settings panels here

        frame.setSize(300, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true

        YapGame.getInstance().window.focus()
    }

    private fun addEntitySettings(frame: JFrame, constraints: GridBagConstraints) {
        val specificSettingsPanel = JPanel()
        specificSettingsPanel.layout = GridBagLayout()
        val specificConstraints = GridBagConstraints()
        specificConstraints.insets = Insets(5, 5, 5, 5)

        val items = mutableListOf(-1)
        items.addAll(StaticEntities.values().map { it.ordinal })
        val finalItems = items.map { ComboItem(it) }.toTypedArray()
        entityTypeCombo = JComboBox(finalItems)
        entityTypeCombo?.addActionListener {
            specificSettingsPanel.removeAll()
            if ((entityTypeCombo?.selectedItem as ComboItem).id == -1) {
                addTextureSelection(specificSettingsPanel, specificConstraints)
            } else {
                addPitchAndYaw(specificSettingsPanel, specificConstraints)
            }
            frame.pack()
        }
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridwidth = 2
        constraints.gridx = 0
        constraints.gridy = 1
        frame.add(entityTypeCombo, constraints)

        addTextureSelection(specificSettingsPanel, specificConstraints)
        constraints.fill = GridBagConstraints.BOTH
        constraints.gridwidth = 2
        constraints.gridx = 0
        constraints.gridy = 2
        frame.add(specificSettingsPanel, constraints)
    }

    private fun addTextureSelection(panel: JPanel, constraints: GridBagConstraints) {
        val textFieldX = JTextField(selectedTextureIndex.x.toString())
        textFieldX.columns = 7
        textFieldX.isEditable = true
        textFieldX.document.addDocumentListener(CustomDocumentListener {
            try {
                selectedTextureIndex.x = textFieldX.text.toInt()
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridy = 0
        panel.add(textFieldX, constraints)

        val textFieldY = JTextField(selectedTextureIndex.y.toString())
        textFieldY.columns = 7
        textFieldY.isEditable = true
        textFieldY.document.addDocumentListener(CustomDocumentListener {
            try {
                selectedTextureIndex.y = textFieldY.text.toInt()
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 0
        panel.add(textFieldY, constraints)
    }

    private fun addPitchAndYaw(panel: JPanel, constraints: GridBagConstraints) {
        val pitchLabel = JLabel("Pitch")
        constraints.gridx = 0
        constraints.gridy = 0
        panel.add(pitchLabel, constraints)

        val pitch = JTextField(rotation.x.toString())
        pitch.columns = 7
        pitch.isEditable = true
        pitch.document.addDocumentListener(CustomDocumentListener {
            try {
                rotation.x = pitch.text.toFloat()
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 0
        panel.add(pitch, constraints)

        val yawLabel = JLabel("Yaw")
        constraints.gridx = 0
        constraints.gridy = 1
        panel.add(yawLabel, constraints)

        val yaw = JTextField(rotation.y.toString())
        yaw.columns = 7
        yaw.isEditable = true
        yaw.document.addDocumentListener(CustomDocumentListener {
            try {
                rotation.y = yaw.text.toFloat()
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 1
        panel.add(yaw, constraints)
    }

    private fun addSaveLoadButtons(frame: JFrame, constraints: GridBagConstraints) {
        val loadLevelBtn = JButton("Load Level")
        loadLevelBtn.addActionListener(this::loadLevel)
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridy = 0
        frame.add(loadLevelBtn, constraints)

        val saveLevelBtn = JButton("Save Level")
        saveLevelBtn.addActionListener(this::saveLevel)
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 0
        frame.add(saveLevelBtn, constraints)
    }

    private fun loadLevel(e: ActionEvent) {
        val fc = createLevelFileChooser()
        val returnVal = fc.showOpenDialog(frame)
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return
        }

        val file = fc.selectedFile
        LevelUtils.loadLevel(file) {
            YapGame.getInstance().entityManager.removeAllEntities()
            YapGame.getInstance().entityManager.addAllEntities(it)
            val camera = YapGame.getInstance().cameraSystem.getCurrentCamera()
            camera?.let {
                YapGame.getInstance().entityManager.addEntity(it)
            }
        }
    }

    private fun saveLevel(e: ActionEvent) {
        val fc = createLevelFileChooser()
        val returnValue = fc.showSaveDialog(frame)
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return
        }

        val file = fc.selectedFile
        LevelUtils.saveLevel(file, YapGame.getInstance().entityManager.getEntities(Capability.ALL_CAPABILITIES))
    }

    private fun createLevelFileChooser(): JFileChooser {
        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.currentDirectory = File(".")
        fc.isAcceptAllFileFilterUsed = false
        fc.addChoosableFileFilter(LevelFileFilter())
        return fc
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
        game.cameraSystem.toggleMouseMovementTracking()
        reactToMouseInput = !reactToMouseInput
    }

    private fun removeBlock() {
        clampedPoint?.let { p ->
            normal?.let { n ->
                val game = YapGame.getInstance()
                val removalPoint = Vector3f(p).sub(n)
                // TODO use a spatial query
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
            val entity = if ((entityTypeCombo?.selectedItem as ComboItem).id == -1) {
                BlockEntity.singleTextureBlock(it, selectedTextureIndex)
            } else {
                val id = (entityTypeCombo?.selectedItem as ComboItem).id
                StaticEntity(StaticEntities.values()[id], it, rotation.x, rotation.y)
            }
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
                        .translate(0.5F, 0.5F, 0.5F)
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
        // TODO use a spatial query
        val boundingBoxes = entities.map {
            TransformedBoundingBox(
                    it.getComponent(),
                    Matrix4f().translate(it.getComponent<PositionComponent>().position)
            )
        }
        val cameraEntity = YapGame.getInstance().cameraSystem.getCurrentCamera()
        var rayStart = Vector3f(0.0F)
        var direction = Vector3f(0.0F, 0.0F, -1.0F)
        cameraEntity?.let {
            rayStart = it.getComponent<PositionComponent>().position
            direction = it.getComponent<RotationComponent>().direction()
        }
        val intersectionResult = intersects(rayStart, direction, boundingBoxes)
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
        val point = Vector3f(intersectionResult.point).add(Vector3f(normal).mul(-0.01F))

        var x = point.x
        x = clamp(x)
        x += if (normal.x == 0.0F) {
            0.0F
        } else {
            normal.x
        }

        var y = point.y
        y = clamp(y)
        y += if (normal.y == 0.0F) {
            0.0F
        } else {
            normal.y
        }

        var z = point.z
        z = clamp(z)
        z += if (normal.z == 0.0F) {
            0.0F
        } else {
            normal.z
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

class CustomDocumentListener(val function: () -> Unit) : DocumentListener {
    override fun changedUpdate(p0: DocumentEvent?) {
        function()
    }

    override fun insertUpdate(p0: DocumentEvent?) {
        function()
    }

    override fun removeUpdate(p0: DocumentEvent?) {
        function()
    }
}
