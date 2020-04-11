package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.entities.StaticEntity
import de.yap.engine.ecs.entities.StaticEntityType
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
import java.awt.event.*
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileFilter
import kotlin.math.floor


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
            StaticEntityType.values()[id].toString()
        }
    }
}

class LevelEditor : ISystem(BoundingBoxComponent::class.java, PositionComponent::class.java) {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private lateinit var frame: JFrame

    private var reactToMouseInput = false

    private var clampedPoint: Vector3f? = null
    private var normal: Vector3f? = null

    private var selectedTextureIndex = Vector2i(0)
    private var rotationInDegrees = Vector3f(0.0F)
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
        items.addAll(StaticEntityType.values().map { it.ordinal })
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
        // pixel size of the texture atlas selection
        val imageSize = 512
        // actual atlas and tile size
        val textureAtlasSize = 1024
        val tileSize = 32

        val width = (imageSize * (tileSize.toFloat() / textureAtlasSize.toFloat())).toInt()
        val height = (imageSize * (tileSize.toFloat() / textureAtlasSize.toFloat())).toInt()
        val img = ImageIO.read(File("models/texture_atlas.png"))
        val subimage = img.getSubimage(0, 0, width, height).getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)
        val imagePreviewLabel = JLabel(ImageIcon(subimage))
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridwidth = 2
        constraints.gridy = 0
        panel.add(imagePreviewLabel, constraints)

        val scaledImage = ImageIcon("models/texture_atlas.png", "Texture Atlas")
                .image
                .getScaledInstance(imageSize, imageSize,  java.awt.Image.SCALE_SMOOTH)
        val textureImage = JLabel(ImageIcon(scaledImage))
        textureImage.addMouseListener(CustomMouseListener { mouseEvent ->
            try {
                val mouseX = mouseEvent!!.x.toFloat()
                val mouseY = mouseEvent.y.toFloat()

                val mousePosPercentageX = mouseX / imageSize.toFloat()
                val mousePosPercentageY = mouseY / imageSize.toFloat()

                val pixelXInAtlas = mousePosPercentageX * textureAtlasSize
                val pixelYInAtlas = mousePosPercentageY * textureAtlasSize

                val textureIndexX = floor(pixelXInAtlas / tileSize).toInt()
                val textureIndexY = floor(pixelYInAtlas / tileSize).toInt()

                selectedTextureIndex.x = textureIndexX
                selectedTextureIndex.y = textureIndexY

                val newSubimage = ImageIO.read(File("models/texture_atlas.png"))
                        .getSubimage(textureIndexX * tileSize, textureIndexY * tileSize, tileSize, tileSize)
                        .getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)
                imagePreviewLabel.icon = ImageIcon(newSubimage)

            } catch (e: NullPointerException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridwidth = 2
        constraints.gridy = 1
        panel.add(textureImage, constraints)
    }

    private fun addPitchAndYaw(panel: JPanel, constraints: GridBagConstraints) {
        val pitchLabel = JLabel("Pitch")
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 1
        constraints.gridheight = 1
        panel.add(pitchLabel, constraints)

        val pitchTF = JTextField(rotationInDegrees.x.toString())
        val pitchSlider = JSlider(0, 360, 0)
        pitchTF.columns = 5
        pitchTF.isEditable = true
        pitchTF.addKeyListener(CustomKeyListener {
            try {
                rotationInDegrees.x = pitchTF.text.toFloat()
                SwingUtilities.invokeLater {
                    pitchSlider.value = rotationInDegrees.x.toInt()
                }
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 0
        panel.add(pitchTF, constraints)

        pitchSlider.minimum = 0
        pitchSlider.maximum = 360
        val pitchFunction: () -> Unit = {
            rotationInDegrees.x = pitchSlider.value.toFloat()
            SwingUtilities.invokeLater {
                pitchTF.text = rotationInDegrees.x.toString()
            }
        }
        pitchSlider.addMouseListener(CustomMouseListener { pitchFunction() })
        pitchSlider.addMouseMotionListener(CustomMouseMotionListener(pitchFunction))
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 2
        constraints.gridy = 0
        panel.add(pitchSlider, constraints)

        val yawLabel = JLabel("Yaw")
        constraints.gridx = 0
        constraints.gridy = 1
        panel.add(yawLabel, constraints)

        val yawTF = JTextField(rotationInDegrees.y.toString())
        val yawSlider = JSlider(0, 360, 0)
        yawTF.columns = 5
        yawTF.isEditable = true
        yawTF.addKeyListener(CustomKeyListener {
            try {
                rotationInDegrees.y = yawTF.text.toFloat()
                SwingUtilities.invokeLater {
                    yawSlider.value = rotationInDegrees.y.toInt()
                }
            } catch (e: NumberFormatException) {
                // ignore
            }
        })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 1
        panel.add(yawTF, constraints)

        val yawFunction = {
            rotationInDegrees.y = yawSlider.value.toFloat()
            SwingUtilities.invokeLater {
                yawTF.text = rotationInDegrees.y.toString()
            }
        }
        yawSlider.addMouseMotionListener(CustomMouseMotionListener(yawFunction))
        yawSlider.addMouseListener(CustomMouseListener { yawFunction() })
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 2
        constraints.gridy = 1
        panel.add(yawSlider, constraints)
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

        if (event.key == GLFW.GLFW_KEY_R && event.action == GLFW.GLFW_RELEASE) {
            rotationInDegrees.y += 90.0F
            // TODO update the UI accordingly
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
            val entity = createSelectedEntity(it)
            YapGame.getInstance().entityManager.addEntity(entity)
        }
    }

    private fun createSelectedEntity(clampedPoint: Vector3f): Entity {
        return if ((entityTypeCombo?.selectedItem as ComboItem).id == -1) {
            BlockEntity.singleTextureBlock(clampedPoint, selectedTextureIndex)
        } else {
            val id = (entityTypeCombo?.selectedItem as ComboItem).id
            val pitch = Math.toRadians(rotationInDegrees.x.toDouble()).toFloat()
            val yaw = Math.toRadians(rotationInDegrees.y.toDouble()).toFloat()
            StaticEntity(StaticEntityType.values()[id], clampedPoint, pitch, yaw)
        }
    }

    override fun render(entities: List<Entity>) {
        renderCrosshair()
        renderSelectedBlock()
    }

    private fun renderSelectedBlock() {
        clampedPoint?.let {
            val entity = createSelectedEntity(it)
            val meshComponent = entity.getComponent<MeshComponent>()
            val rotationComponent = entity.getComponent<RotationComponent>()
            val renderer = YapGame.getInstance().renderer

            val isBlock = entity is BlockEntity
            renderer.wireframe(isBlock) {
                val transformation = Matrix4f()
                        .translate(it)
                        .translate(meshComponent.offset)
                        .rotate(rotationComponent.yaw, Vector3f(0F, 1F, 0F))
                        .rotate(rotationComponent.pitch, Vector3f(0F, 0F, 1F))
                renderer.mesh(meshComponent.mesh, transformation)
            }

            if (isBlock) {
                return@let
            }
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
        val cameraEntity = YapGame.getInstance().cameraSystem.getCurrentCamera()
                ?: return
        if (cameraEntity.getComponent<CameraComponent>().type != CameraType.FIRST_PERSON) {
            return
        }

        // TODO use a spatial query
        val boundingBoxes = entities.map {
            val position = it.getComponent<PositionComponent>().position
            TransformedBoundingBox(
                    it.getComponent(),
                    Matrix4f().translate(position)
            )
        }

        val rayStart = cameraEntity.getComponent<PositionComponent>().position
        val cameraOffset = cameraEntity.getComponent<CameraComponent>().offset
        rayStart.add(cameraOffset)

        val direction = cameraEntity.getComponent<RotationComponent>().direction()

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
        val point = Vector3f(intersectionResult.point)
                .add(Vector3f(normal).mul(-0.1F))

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

class CustomKeyListener(val function: () -> Unit) : KeyListener {
    override fun keyTyped(e: KeyEvent?) {
    }

    override fun keyPressed(e: KeyEvent?) {
    }

    override fun keyReleased(e: KeyEvent?) {
        function()
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

class CustomMouseMotionListener(val function: () -> Unit) : MouseMotionListener {
    override fun mouseMoved(e: MouseEvent?) {
    }

    override fun mouseDragged(e: MouseEvent?) {
        function()
    }
}

class CustomMouseListener(val function: (MouseEvent?) -> Unit) : MouseListener {
    override fun mouseReleased(mouseEvent: MouseEvent?) {
    }

    override fun mouseEntered(mouseEvent: MouseEvent?) {
    }

    override fun mouseClicked(mouseEvent: MouseEvent?) {
        function(mouseEvent)
    }

    override fun mouseExited(mouseEvent: MouseEvent?) {
    }

    override fun mousePressed(mouseEvent: MouseEvent?) {
    }
}
