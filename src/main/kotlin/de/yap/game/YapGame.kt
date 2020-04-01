package de.yap.game

import de.yap.engine.IGameLogic
import de.yap.engine.debug.DebugFontTexture
import de.yap.engine.debug.DebugFrameTiming
import de.yap.engine.debug.DebugInterface
import de.yap.engine.ecs.EntityManager
import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.WindowResizeEvent
import de.yap.engine.ecs.entities.PlayerEntity
import de.yap.engine.ecs.systems.FirstPersonCamera
import de.yap.engine.ecs.systems.LevelEditor
import de.yap.engine.ecs.systems.MeshSystem
import de.yap.engine.ecs.systems.ShowComponentInfoSystem
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Window
import de.yap.engine.mesh.Mesh
import de.yap.engine.util.FIELD_OF_VIEW
import de.yap.engine.util.Z_FAR
import de.yap.engine.util.Z_NEAR
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

    private var roomWireframe = false

    private var roomMeshes: List<Mesh> = emptyList()
    private val scale = 2.0F
    private val negativeScaleHalf = -0.5F * scale
    private val position = Vector3f(negativeScaleHalf)
    private val roomTransformation = Matrix4f().translate(position).scale(scale)

    override fun init(window: Window) {
        this.window = window

        renderer.init()
        fontRenderer.init()

        roomMeshes = Mesh.fromFile("models/scene.obj")

        initSystems()

        initEntities()

        // we need to fire this ourselves, because we are not registered to listen to events at the time when window actually fires this
        entityManager.fireEvent(WindowResizeEvent(window.width, window.height))
    }

    private fun initSystems() {
        entityManager.registerEventListener(this)
        entityManager.registerSystem(LevelEditor())
        entityManager.registerSystem(FirstPersonCamera())
        entityManager.registerSystem(MeshSystem())
        entityManager.registerSystem(ShowComponentInfoSystem())
        entityManager.registerSystem(DebugInterface())
        entityManager.registerSystem(DebugFrameTiming())
        entityManager.registerSystem(DebugFontTexture())
        entityManager.init()
    }

    private fun initEntities() {
        entityManager.addEntity(PlayerEntity(Vector3f(0.5F, 0.0F, 3.0F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F), hasInput = true))
        entityManager.addEntity(PlayerEntity(Vector3f(0.0F, 1.0F, 2.0F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F), hasInput = false))

        val levelGenerator = LevelGenerator()
        for ((pos, entity) in levelGenerator.generateLevelEntities()){
            entityManager.addEntity(entity)
        }
        val position = Vector3f(0.5F, 0.5F, 0.5F)
        entityManager.addEntity(BlockEntity(position))
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
        renderer.shader3D.setUniform("lightPos", Vector3f(1.0f, 1.0f, 1.0f))
        renderer.shader3D.setUniform("lightColor", Vector3f(0.075f, 0.075f, 0.075f))

        entityManager.render()
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

    private fun renderRoom() {
        renderer.wireframe(roomWireframe) {
            for (roomMesh in roomMeshes) {
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.mesh(roomMesh, roomTransformation, color)
            }
        }
    }
}
