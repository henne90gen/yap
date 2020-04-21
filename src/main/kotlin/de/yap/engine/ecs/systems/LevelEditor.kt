package de.yap.engine.ecs.systems

import de.yap.engine.ecs.*
import de.yap.engine.ecs.entities.BlockEntity
import de.yap.engine.ecs.entities.Entity
import de.yap.engine.util.CollisionUtils
import de.yap.engine.util.TransformedBoundingBox
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.awt.event.*
import java.io.File
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

class LevelEditor : ISystem(BoundingBoxComponent::class.java, PositionComponent::class.java) {

    private lateinit var settings: LevelEditorSettings

    private var reactToMouseInput = false

    // the block that the player points at with his crosshair
    private var clampedPoint: Vector3f? = null
    private var normal: Vector3f? = null

    override fun init() {
        settings = LevelEditorSettings()
        settings.init()

        YapGame.getInstance().window.focus()
    }

    @Subscribe
    fun onWindowClose(event: WindowCloseEvent) {
        settings.close()
    }

    @Subscribe
    fun onMouseClick(event: MouseClickEvent) {
        if (!reactToMouseInput) {
            return
        }

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_1 && event.action == GLFW.GLFW_RELEASE) {
            leftMouseClick()
        } else if (event.button == GLFW.GLFW_MOUSE_BUTTON_2 && event.action == GLFW.GLFW_RELEASE) {
            rightMouseClick()
        }
    }

    @Subscribe
    fun onKeyboardPress(event: KeyboardEvent) {
        if (event.key == GLFW.GLFW_KEY_LEFT_ALT && event.action == GLFW.GLFW_RELEASE) {
            toggleReactToMouseInput()
        }

        if (event.key == GLFW.GLFW_KEY_R && event.action == GLFW.GLFW_RELEASE) {
            settings.rotate(0.0F, (Math.PI / 2.0).toFloat())
        }

        if (event.key == GLFW.GLFW_KEY_P && event.action == GLFW.GLFW_RELEASE) {
            clampedPoint?.let {
                val newGoal = Vector3f(it)
                        .add(normal)
                settings.addNewGoal(newGoal)
            }
        }
    }

    private fun toggleReactToMouseInput() {
        val game = YapGame.getInstance()
        game.window.toggleMouseVisibility()
        game.cameraSystem.toggleMouseMovementTracking()
        reactToMouseInput = !reactToMouseInput
    }

    private fun leftMouseClick() {
        clampedPoint?.let {
            if (settings.createSelectedEntity(it) == null) {
                selectBlock(it)
            } else {
                removeBlock(it)
            }
        }
    }

    private fun removeBlock(p: Vector3f) {
        val game = YapGame.getInstance()
        // TODO use a spatial query
        val entities = game.entityManager.getEntities(capability)
        for (entity in entities) {
            val position = entity.getComponent<PositionComponent>().position
            if (p == position) {
                game.entityManager.removeEntity(entity)
                break
            }
        }
    }

    private fun selectBlock(p: Vector3f) {
        val game = YapGame.getInstance()
        // TODO use a spatial query
        val entities = game.entityManager.getEntities(capability)
        for (entity in entities) {
            val position = entity.getComponent<PositionComponent>().position
            if (p == position) {
                settings.updateSelectedEntity(entity)
                break
            }
        }
    }

    private fun rightMouseClick() {
        clampedPoint?.let { p ->
            if (settings.createSelectedEntity(p) == null) {
                // ignore
            } else {
                normal?.let { n ->
                    val entity = settings.createSelectedEntity(Vector3f(p)
                            .add(n))
                    entity?.let {
                        YapGame.getInstance().entityManager.addEntity(it)
                    }
                }
            }
        }
    }

    override fun render(entities: List<Entity>) {
        renderCrosshair()
        maybeRenderSelectedEntity()
    }

    private fun maybeRenderSelectedEntity() {
        clampedPoint?.let { p ->
            normal?.let { n ->
                val entity = settings.createSelectedEntity(Vector3f(p).add(n))
                if (entity == null) {
                    renderSelectionPreview(p, n)
                } else {
                    renderEntityPreview(entity, p, n)
                }
            }
        }
    }

    private fun renderSelectionPreview(p: Vector3f, n: Vector3f) {
        val renderer = YapGame.getInstance().renderer

        renderer.wireframe {
            val transformation = Matrix4f()
                    .translate(p)
                    .translate(0.5F, 0.5F, 0.5F)
                    .scale(1.05F, 1.05F, 1.05F)
            renderer.cube(transformation)
        }
    }

    private fun renderEntityPreview(entity: Entity, p: Vector3f, n: Vector3f) {
        val meshComponent = entity.getComponent<MeshComponent>()
        val rotationComponent = entity.getComponent<RotationComponent>()
        val renderer = YapGame.getInstance().renderer

        val wireframeOn = entity is BlockEntity && settings.selectedEntityType != SelectedEntityType.TRIGGER
        renderer.wireframe(wireframeOn) {
            val transformation = Matrix4f()
                    .translate(p)
                    .translate(n)
                    .translate(meshComponent.offset)
                    .rotate(rotationComponent.yaw, Vector3f(0F, 1F, 0F))
                    .rotate(rotationComponent.pitch, Vector3f(0F, 0F, 1F))
            renderer.mesh(meshComponent.mesh, transformation)
        }

        if (wireframeOn) {
            return
        }
        renderer.wireframe {
            val transformation = Matrix4f()
                    .translate(p)
                    .translate(n)
                    .translate(0.5F, 0.5F, 0.5F)
            val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
            renderer.cube(transformation, color)
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
            val position = it.getComponent<PositionComponent>().position
            TransformedBoundingBox(
                    it.getComponent(),
                    Matrix4f().translate(position)
            )
        }

        val intersectionResult = CollisionUtils.rayCastFromCamera(boundingBoxes)
        if (intersectionResult.hasValue()) {
            clampedPoint = intersectionResult.point
            normal = intersectionResult.normal
        } else {
            clampedPoint = null
            normal = null
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
