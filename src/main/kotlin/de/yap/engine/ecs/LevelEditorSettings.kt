package de.yap.engine.ecs

import de.yap.engine.ecs.entities.*
import de.yap.engine.ecs.systems.CustomKeyListener
import de.yap.engine.ecs.systems.CustomMouseListener
import de.yap.engine.ecs.systems.CustomMouseMotionListener
import de.yap.engine.ecs.systems.LevelFileFilter
import de.yap.engine.util.LevelUtils
import de.yap.game.YapGame
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
    BLOCK,
    STATIC,
    DYNAMIC
}

enum class EditorMode {
    CREATE,
    EDIT
}

class LevelEditorSettings {

    private lateinit var frame: JFrame

    var editorMode = EditorMode.CREATE
    var selectedTextureIndex = Vector2i(0)
    var rotationInDegrees = Vector3f(0.0F)
    var selectedEntityType = SelectedEntityType.BLOCK
    var staticEntityTypeCombo: JComboBox<StaticEntityType>? = null
    var dynamicEntityTypeCombo: JComboBox<DynamicEntityType>? = null

    fun init() {
        frame = JFrame("Settings")
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)

        val saveLoadButtons = createSaveLoadButtons()
        frame.add(saveLoadButtons)

        val editorMode = createEditorMode()
        frame.add(editorMode)

        val entitySettings = createEntitySettings()
        frame.add(entitySettings)

        frame.setSize(200, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
        frame.isVisible = true
    }

    private fun createEditorMode(): JPanel {
        val panel = JPanel()

        val buttonGroup = ButtonGroup()

        for (mode in EditorMode.values()) {
            val active = mode == editorMode
            val button = JRadioButton(mode.name, active)
            button.addActionListener {
                editorMode = mode
            }
            panel.add(button)
            buttonGroup.add(button)
        }

        return panel
    }

    private fun createEntitySettings(): JComponent {
        val tabbedPane = JTabbedPane()
        tabbedPane.addChangeListener {
            selectedEntityType = when (tabbedPane.selectedIndex) {
                0 -> SelectedEntityType.BLOCK
                1 -> SelectedEntityType.STATIC
                2 -> SelectedEntityType.DYNAMIC
                else -> SelectedEntityType.BLOCK
            }
        }

        val blockPanel = createBlockPanel()
        tabbedPane.addTab("Blocks", null, blockPanel, "Blocks")
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1)

        val staticEntityPanel = createStaticEntityPanel()
        tabbedPane.addTab("Static Entities", null, staticEntityPanel, "Static Entities")
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2)

        val dynamicEntityPanel = createDynamicEntityPanel()
        tabbedPane.addTab("Dynamic Entities", null, dynamicEntityPanel, "Dynamic Entities")
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3)

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

    fun createSelectedEntity(clampedPoint: Vector3f): Entity {
        return when (selectedEntityType) {
            SelectedEntityType.BLOCK -> BlockEntity.singleTextureBlock(clampedPoint, selectedTextureIndex)
            SelectedEntityType.STATIC -> {
                val id = staticEntityTypeCombo?.selectedItem as StaticEntityType
                val pitch = Math.toRadians(rotationInDegrees.x.toDouble()).toFloat()
                val yaw = Math.toRadians(rotationInDegrees.y.toDouble()).toFloat()
                StaticEntity(id, clampedPoint, pitch, yaw)
            }
            SelectedEntityType.DYNAMIC -> TODO()
        }
    }

    fun close() {
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }
}
