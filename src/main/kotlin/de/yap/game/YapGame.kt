package de.yap.game

import de.yap.engine.*
import de.yap.engine.debug.DebugFontTexture
import de.yap.engine.debug.DebugInterface
import de.yap.engine.ecs.*
import de.yap.engine.ecs.systems.CameraSystem
import de.yap.engine.ecs.systems.MeshSystem
import de.yap.engine.ecs.systems.ShowComponentInfoSystem
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Text
import de.yap.engine.mesh.Mesh
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame private constructor() : IGameLogic {

    companion object {
        private val log: Logger = LogManager.getLogger()

        private val yapGame: YapGame = YapGame()

        fun getInstance(): YapGame {
            return yapGame
        }
    }

    lateinit var window: Window

    val entityManager = EntityManager()

    val renderer = Renderer()
    val fontRenderer = FontRenderer()

    var view: Matrix4f = Matrix4f()
    var projection: Matrix4f = Matrix4f()

    private var cameraRayStart = Vector3f()
    private var cameraRayResult = IntersectionResult()

    private var roomWireframe = false

    private var roomMeshes: List<Mesh> = emptyList()
    private val scale = 2.0F
    private val negativeScaleHalf = -0.5F * scale
    private val position = Vector3f(negativeScaleHalf)
    private val roomTransformation = Matrix4f().translate(position).scale(scale)

    private var text: Text? = null

    override fun init(window: Window) {
        this.window = window

        renderer.init()
        fontRenderer.init()

        roomMeshes = Mesh.fromFile("models/scene.obj")

        text = createText()

        initSystems()

        initEntities()

        // we need to fire this ourselves, because we are not registered to listen to events at the time when window actually fires this
        entityManager.fireEvent(WindowResizeEvent(window.width, window.height))
    }

    private fun initSystems() {
        entityManager.registerEventListener(this)
        entityManager.registerSystem(CameraSystem())
        entityManager.registerSystem(MeshSystem())
        entityManager.registerSystem(ShowComponentInfoSystem())
        entityManager.registerSystem(DebugInterface())
        entityManager.registerSystem(DebugFontTexture())
        entityManager.init()
    }

    private fun initEntities() {
        entityManager.addEntity(PlayerEntity(Vector3f(0.5F, 0.0F, 3.0F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F), hasInput = true))
        entityManager.addEntity(PlayerEntity(Vector3f(-2.9F, 0.0F, 3.9F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F), hasInput = false))

        for (x in 0..10) {
            for (y in 0..10) {
                entityManager.addEntity(BlockEntity(Vector3f(x.toFloat(), 0.0F, y.toFloat())))
            }
        }
    }

    private fun createText(): Text {
        var value = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vitae purus dolor. Mauris pellentesque commodo nulla, sit amet euismod sapien viverra ut. Cras commodo euismod turpis, ac lobortis augue. Nam consequat sodales quam ac porttitor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc non est iaculis, posuere diam a, suscipit nibh. Fusce nec erat vel sapien dictum pulvinar eu porttitor leo. Nulla finibus dolor turpis, eu sagittis risus tincidunt sed. Ut convallis augue massa, vel dapibus mauris scelerisque eget. Duis sollicitudin vulputate augue, tincidunt ornare dolor feugiat at."
        value = value.replace(". ", ".\n")
        val color = Vector4f(0.0F, 0.5F, 0.75F, 1.0F)
        val transform = Matrix4f().translate(-2.0F, 2.0F, 0.0F)
        return Text(value, fontRenderer.font, transform, color)
    }

    @Subscribe
    fun keyboardEvent(event: KeyboardEvent) {
        fun keyReleased(key: Int): Boolean {
            return event.key == key && event.action == GLFW.GLFW_RELEASE
        }

        if (keyReleased(GLFW.GLFW_KEY_ESCAPE)) {
            window.close()
        }
        if (keyReleased(GLFW.GLFW_KEY_F)) {
            roomWireframe = !roomWireframe
        }
    }

    override fun update(interval: Float) {
        entityManager.update(interval)
    }

    override fun render() {
        renderer.clear()

        renderer.shader3D.setUniform("view", view)
        renderer.shader3D.setUniform("projection", projection)
        renderer.shader3D.setUniform("color", Vector4f(1.0F))
        renderer.shader3D.setUniform("lightPos", Vector3f(2.0f, 0.0f, 4.0f))
        renderer.shader3D.setUniform("lightColor", Vector3f(0.5f, 0.3f, 0.2f))

        renderCoordinateSystemAxis()
        renderRoom()
        renderTextInScene(text)
//        renderTextOnScreen(text)

        entityManager.render()
    }

    private fun renderTextOnScreen(text: Text?) {
        val color = Vector4f(0.5F, 0.75F, 0.0F, 1.0F)
        val transform = Matrix4f().translate(-window.aspectRatio(), 0.85F, 0.0F)
        text?.value?.let { fontRenderer.string(it, transform, color) }
    }

    @Subscribe
    fun onWindowResize(event: WindowResizeEvent) {
        glViewport(0, 0, window.width, window.height)
        fontRenderer.aspectRatio = window.aspectRatio()
        projection = Matrix4f()
                .perspective(
                        Math.toRadians(FIELD_OF_VIEW).toFloat(),
                        window.aspectRatio(),
                        Z_NEAR, Z_FAR
                )
    }

    private fun renderTextInScene(text: Text?) {
        if (text == null) {
            return
        }

        fontRenderer.stringInScene(text, view, projection)
    }

    private fun renderRoom() {
        renderer.wireframe(roomWireframe) {
            for (roomMesh in roomMeshes) {
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.mesh(roomMesh, roomTransformation, color)
            }
        }
    }

    private fun renderCoordinateSystemAxis() {
        renderer.cube(Matrix4f().translate(Vector3f(0.0F, 0.0F, 0.0F)).scale(0.1F))

        renderer.cube(Matrix4f().translate(Vector3f(1.0F, 0.0F, 0.0F)).scale(0.1F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
        renderer.cube(Matrix4f().translate(Vector3f(0.0F, 1.0F, 0.0F)).scale(0.1F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
        renderer.cube(Matrix4f().translate(Vector3f(0.0F, 0.0F, 1.0F)).scale(0.1F), Vector4f(0.0F, 0.0F, 1.0F, 1.0F))

        renderer.line(Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
        renderer.line(Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
        renderer.line(Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F), Vector4f(0.0F, 0.0F, 1.0F, 1.0F))
    }
}
