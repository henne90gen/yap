package de.yap.engine.debug

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.WindowCloseEvent
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.entities.PlayerEntity
import de.yap.engine.ecs.entities.StaticEntity
import de.yap.engine.ecs.systems.ISystem
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.awt.event.ItemEvent
import java.awt.event.WindowEvent
import javax.swing.*


class DebugInterface : ISystem() {

    private lateinit var frame: JFrame

    private lateinit var entitiesCount: JLabel
    private lateinit var blockEntitiesCount: JLabel
    private lateinit var staticEntitiesCount: JLabel
    private lateinit var playerEntitiesCount: JLabel

    private lateinit var fontHeight: JLabel
    private lateinit var bitmapWidth: JLabel
    private lateinit var bitmapHeight: JLabel
    private lateinit var scaleForPixelHeight: JLabel
    private lateinit var ascent: JLabel
    private lateinit var descent: JLabel
    private lateinit var lineGap: JLabel
    private lateinit var firstChar: JLabel

    private var showCoordinateSystem = false

    override fun init() {
        frame = JFrame("Debug Settings")
        val debugToggles = createDebugToggles()
        frame.add(debugToggles)

        val fontInfo = createFontInformation()
        frame.add(fontInfo)

        val entitiesInfo = createEntitiesInformation()
        frame.add(entitiesInfo)

        frame.setSize(300, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)
        frame.isVisible = true
        frame.setLocation(1600, 100)
    }

    private fun createFontInformation(): JPanel {
        val infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)

        fontHeight = JLabel()
        infoPanel.add(fontHeight)

        bitmapWidth = JLabel()
        infoPanel.add(bitmapWidth)

        bitmapHeight = JLabel()
        infoPanel.add(bitmapHeight)

        scaleForPixelHeight = JLabel()
        infoPanel.add(scaleForPixelHeight)

        ascent = JLabel()
        infoPanel.add(ascent)

        descent = JLabel()
        infoPanel.add(descent)

        lineGap = JLabel()
        infoPanel.add(lineGap)

        firstChar = JLabel()
        infoPanel.add(firstChar)

        return infoPanel
    }

    private fun createEntitiesInformation(): JPanel {
        val infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)

        entitiesCount = JLabel("Entities")
        infoPanel.add(entitiesCount)

        blockEntitiesCount = JLabel("Block Entities")
        infoPanel.add(blockEntitiesCount)

        staticEntitiesCount = JLabel("Static Entities")
        infoPanel.add(staticEntitiesCount)

        playerEntitiesCount = JLabel("Player Entities")
        infoPanel.add(playerEntitiesCount)

        return infoPanel
    }

    private fun createDebugToggles(): JPanel {
        val debugToggles = JPanel()
        debugToggles.layout = BoxLayout(debugToggles, BoxLayout.Y_AXIS)

        val cpu = JCheckBox("CPU Info", YapGame.getInstance().debugCPU.enabled)
        cpu.addItemListener { YapGame.getInstance().debugCPU.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(cpu)

        val memory = JCheckBox("Memory Info", YapGame.getInstance().debugMemory.enabled)
        memory.addItemListener { YapGame.getInstance().debugMemory.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(memory)

        val frameTiming = JCheckBox("Frame Timings", YapGame.getInstance().debugFrameTiming.enabled)
        frameTiming.addItemListener { YapGame.getInstance().debugFrameTiming.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(frameTiming)

        val boundingBox = JCheckBox("Bounding Boxes", YapGame.getInstance().debugBoundingBox.enabled)
        boundingBox.addItemListener { YapGame.getInstance().debugBoundingBox.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(boundingBox)

        val fontTexture = JCheckBox("Font Debug", YapGame.getInstance().debugFont.enabled)
        fontTexture.addItemListener { YapGame.getInstance().debugFont.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(fontTexture)

        val coordinateSystem = JCheckBox("Show Coordinate System", showCoordinateSystem)
        coordinateSystem.addItemListener { showCoordinateSystem = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(coordinateSystem)

        return debugToggles
    }

    @Subscribe
    fun onWindowClose(event: WindowCloseEvent) {
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }

    override fun render(entities: List<Entity>) {
        entitiesCount.text = "${entities.size} Entities"

        var blockEntities = 0
        var staticEntities = 0
        var playerEntities = 0
        for (entity in entities) {
            when (entity) {
                is BlockEntity -> blockEntities++
                is StaticEntity -> staticEntities++
                is PlayerEntity -> playerEntities++
            }
        }
        blockEntitiesCount.text = "$blockEntities Block Entities"
        staticEntitiesCount.text = "$staticEntities Static Entities"
        playerEntitiesCount.text = "$playerEntities Player Entities"

        val font = YapGame.getInstance().fontRenderer.font
        fontHeight.text = "Font Height: ${font.fontHeight}"
        bitmapWidth.text = "Bitmap Width: ${font.bitmapWidth}"
        bitmapHeight.text = "Bitmap Height: ${font.bitmapHeight}"
        scaleForPixelHeight.text = "Scale for Pixel Height: ${font.scaleForPixelHeight}"
        ascent.text = "Ascent: ${font.ascent}"
        descent.text = "Descent: ${font.descent}"
        lineGap.text = "Line Gap: ${font.lineGap}"
        firstChar.text = "First Char: ${font.firstChar}"

        if (!showCoordinateSystem) {
            return
        }
        renderCoordinateSystem()
    }

    private fun renderCoordinateSystem() {
        val renderer = YapGame.getInstance().renderer
        val currentCamera = YapGame.getInstance().cameraSystem.getCurrentCamera()
        currentCamera?.let {
            val positionComponent = currentCamera.getComponent<PositionComponent>()
            val rotationComponent = currentCamera.getComponent<RotationComponent>()

            val position = positionComponent.position
            val direction = rotationComponent.direction()
            val transformation = Matrix4f()
                    .translate(position)
                    .translate(direction)
                    .scale(0.075F)
            val origin = Vector3f(0.0F, 0.0F, 0.0F)
            val xEnd = Vector3f(1.0F, 0.0F, 0.0F)
            val yEnd = Vector3f(0.0F, 1.0F, 0.0F)
            val zEnd = Vector3f(0.0F, 0.0F, 1.0F)

            transformation.transformPosition(origin)
            transformation.transformPosition(xEnd)
            transformation.transformPosition(yEnd)
            transformation.transformPosition(zEnd)

            renderer.line(origin, xEnd, Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
            renderer.line(origin, yEnd, Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
            renderer.line(origin, zEnd, Vector4f(0.0F, 0.0F, 1.0F, 1.0F))
        }
    }
}
