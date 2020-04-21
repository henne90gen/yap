package de.yap.engine.ecs

import de.yap.engine.ecs.entities.*
import de.yap.engine.ecs.systems.*
import de.yap.engine.graphics.TRIGGER_TEXTURE_COORDS
import de.yap.engine.util.LevelUtils
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.joml.Vector2i
import org.joml.Vector3f
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.floor


enum class SelectedEntityType {
    NONE,
    BLOCK,
    STATIC,
    DYNAMIC,
    TRIGGER
}

class LevelEditorSettings {

    companion object {
        private val log = LogManager.getLogger()
    }

    private lateinit var frame: JFrame

    var selectedTextureIndex = Vector2i(0)
    var rotationInDegrees = Vector3f(0.0F)
    var selectedEntityType = SelectedEntityType.BLOCK
    var staticEntityTypeCombo: JComboBox<StaticEntityType>? = null
    var dynamicEntityTypeCombo: JComboBox<DynamicEntityType>? = null
    var triggerTypeCombo: JComboBox<TriggerType>? = null
    var editPanel: JPanel? = null

    private var selectedEntity: Entity? = null

    fun init() {
        frame = JFrame("Settings")
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        val saveLoadButtons = createSaveLoadButtons()
        frame.add(saveLoadButtons)

        val tabs = createTabs()
        frame.add(tabs)

        frame.setSize(200, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
    }

    private fun createTabs(): JComponent {
        val tabbedPane = JTabbedPane()
        tabbedPane.addChangeListener {
            selectedEntityType = when (tabbedPane.selectedIndex) {
                0 -> SelectedEntityType.NONE
                1 -> SelectedEntityType.BLOCK
                2 -> SelectedEntityType.STATIC
                3 -> SelectedEntityType.DYNAMIC
                4 -> SelectedEntityType.TRIGGER
                else -> SelectedEntityType.BLOCK
            }
        }

        editPanel = JPanel()
        tabbedPane.addTab("Edit", null, editPanel, "Edit")
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1)

        val blockPanel = createBlockPanel()
        tabbedPane.addTab("Blocks", null, blockPanel, "Blocks")
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2)

        val staticEntityPanel = createStaticEntityPanel()
        tabbedPane.addTab("Static Entities", null, staticEntityPanel, "Static Entities")
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3)

        val dynamicEntityPanel = createDynamicEntityPanel()
        tabbedPane.addTab("Dynamic Entities", null, dynamicEntityPanel, "Dynamic Entities")
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4)

        val triggerEntityPanel = createTriggerEntityPanel()
        tabbedPane.addTab("Triggers", null, triggerEntityPanel, "Triggers")
        tabbedPane.setMnemonicAt(4, KeyEvent.VK_5)

        return tabbedPane
    }

    private fun createBlockPanel(): JPanel {
        val panel = JPanel()
        val constraints = GridBagConstraints()

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
                .getScaledInstance(imageSize, imageSize, java.awt.Image.SCALE_SMOOTH)
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
                        .getScaledInstance(64, 64, Image.SCALE_SMOOTH)
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

        return panel
    }

    private fun createStaticEntityPanel(): JPanel {
        val panel = JPanel()
        panel.layout = GridBagLayout()
        val constraints = GridBagConstraints()

        val items = StaticEntityType.values()
        staticEntityTypeCombo = JComboBox(items)
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 3
        panel.add(staticEntityTypeCombo, constraints)

        val pitchLabel = JLabel("Pitch")
        constraints.gridx = 0
        constraints.gridy = 1
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
        constraints.gridy = 1
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
        constraints.gridy = 1
        panel.add(pitchSlider, constraints)

        val yawLabel = JLabel("Yaw")
        constraints.gridx = 0
        constraints.gridy = 2
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
        constraints.gridy = 2
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
        constraints.gridy = 2
        panel.add(yawSlider, constraints)
        return panel
    }

    private fun createDynamicEntityPanel(): JPanel {
        val panel = JPanel()

        val items = DynamicEntityType.values()
        dynamicEntityTypeCombo = JComboBox(items)
        panel.add(dynamicEntityTypeCombo)

        return panel
    }

    private fun createTriggerEntityPanel(): JPanel {
        val panel = JPanel()

        val items = TriggerType.values()
        triggerTypeCombo = JComboBox(items)
        panel.add(triggerTypeCombo)

        return panel
    }

    private fun createSaveLoadButtons(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val loadLevelBtn = JButton("Load Level")
        loadLevelBtn.addActionListener(this::loadLevel)
        panel.add(loadLevelBtn)

        val saveLevelBtn = JButton("Save Level")
        saveLevelBtn.addActionListener(this::saveLevel)
        panel.add(saveLevelBtn)

        return panel
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

    fun createSelectedEntity(clampedPoint: Vector3f): Entity? {
        return when (selectedEntityType) {
            SelectedEntityType.NONE -> null
            SelectedEntityType.BLOCK -> BlockEntity.singleTextureBlock(clampedPoint, selectedTextureIndex)
            SelectedEntityType.STATIC -> {
                val id = staticEntityTypeCombo?.selectedItem as StaticEntityType
                val pitch = Math.toRadians(rotationInDegrees.x.toDouble()).toFloat()
                val yaw = Math.toRadians(rotationInDegrees.y.toDouble()).toFloat()
                StaticEntity(id, clampedPoint, pitch, yaw)
            }
            SelectedEntityType.DYNAMIC -> {
                val id = dynamicEntityTypeCombo?.selectedItem as DynamicEntityType
                DynamicEntity(id, clampedPoint)
            }
            SelectedEntityType.TRIGGER -> BlockEntity.singleTextureBlock(clampedPoint, TRIGGER_TEXTURE_COORDS)
        }
    }

    fun close() {
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }

    fun updateSelectedEntity(entity: Entity) {
        selectedEntity = entity
        editPanel?.let {
            it.removeAll()

            it.layout = GridBagLayout()
            val constraints = GridBagConstraints()

            entity.components
                    .map { entry -> entry.value }
                    .sortedBy { component -> component::class.java.simpleName }
                    .forEachIndexed { index, component ->
                        val panel = when (component::class) {
                            PositionComponent::class -> PositionComponentPanel(component as PositionComponent)
                            RotationComponent::class -> RotationComponentPanel(component as RotationComponent)
                            else -> addDefaultComponentEditor(component)
                        }
                        constraints.gridy = index
                        it.add(panel, constraints)
                    }

            frame.pack()
        }
    }

    private fun addDefaultComponentEditor(component: Component): JPanel {
        val panel = JPanel()
        val label = JLabel(component::class.java.simpleName)
        panel.add(label)
        return panel
    }
}

class RotationComponentPanel(val component: RotationComponent) : JPanel() {
    init {
        val label = JLabel(component::class.java.simpleName)
        this.add(label)
    }
}

class PositionComponentPanel(val component: PositionComponent) : JPanel() {
    companion object {
        private val log = LogManager.getLogger()
    }

    init {
        this.layout = GridBagLayout()
        val constraints = GridBagConstraints()

        val label = JLabel("Position: ")
        constraints.gridx = 0
        this.add(label, constraints)

        val position = component.position
        val xTF = JTextField(position.x.toString())
        xTF.columns = 10
        xTF.document.addDocumentListener(CustomDocumentListener {
            try {
                position.x = xTF.text.toFloat()
            } catch (e: NumberFormatException) {
                log.debug("Could not update position.x component")
            }
        })
        constraints.gridx = 1
        this.add(xTF, constraints)

        val yTF = JTextField(position.y.toString())
        yTF.columns = 10
        yTF.document.addDocumentListener(CustomDocumentListener {
            try {
                position.y = yTF.text.toFloat()
            } catch (e: NumberFormatException) {
                log.debug("Could not update position.y component")
            }
        })
        constraints.gridx = 2
        this.add(yTF, constraints)

        val zTF = JTextField(position.z.toString())
        zTF.columns = 10
        zTF.document.addDocumentListener(CustomDocumentListener {
            try {
                position.z = zTF.text.toFloat()
            } catch (e: NumberFormatException) {
                log.debug("Could not update position.z component")
            }
        })
        constraints.gridx = 3
        this.add(zTF, constraints)
    }
}
