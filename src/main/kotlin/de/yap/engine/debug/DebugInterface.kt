package de.yap.engine.debug

import de.yap.engine.ecs.PositionComponent
import de.yap.engine.ecs.RotationComponent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.WindowCloseEvent
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import java.awt.event.ItemEvent
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel


class DebugInterface : ISystem() {

    private lateinit var frame: JFrame

    var enabled = false

    override fun init() {
        frame = JFrame("Debug Settings")
        val debugToggles = createDebugToggles()
        frame.add(debugToggles)

        frame.setSize(300, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)
        frame.isVisible = true
        frame.setLocation(1600, 100)
    }

    private fun createDebugToggles(): JPanel {
        val debugToggles = JPanel()
        debugToggles.layout = BoxLayout(debugToggles, BoxLayout.Y_AXIS)

        val cpu = JCheckBox("CPU Info")
        cpu.addItemListener { YapGame.getInstance().debugCPU.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(cpu)

        val memory = JCheckBox("Memory Info")
        memory.addItemListener { YapGame.getInstance().debugMemory.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(memory)

        val frameTiming = JCheckBox("Frame Timings")
        frameTiming.addItemListener { YapGame.getInstance().debugFrameTiming.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(frameTiming)

        val boundingBox = JCheckBox("Bounding Boxes")
        boundingBox.addItemListener { YapGame.getInstance().debugBoundingBox.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(boundingBox)

        val fontTexture = JCheckBox("Font Texture")
        fontTexture.addItemListener { YapGame.getInstance().debugFontTexture.enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(fontTexture)

        val coordinateSystem = JCheckBox("Show Coordinate System")
        coordinateSystem.addItemListener { enabled = it.stateChange == ItemEvent.SELECTED }
        debugToggles.add(coordinateSystem)

        return debugToggles
    }

    @Subscribe
    fun onWindowClose(event: WindowCloseEvent) {
        frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
    }

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        renderCoordinateSystem()
    }

    private fun renderCoordinateSystem() {
        val renderer = YapGame.getInstance().renderer
        val currentCamera = YapGame.getInstance().firstPersonCamera.getCurrentCamera()
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
