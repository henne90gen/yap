package de.yap.game

import de.yap.engine.IGameLogic
import de.yap.engine.debug.*
import de.yap.engine.ecs.EntityManager
import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.Subscribe
import de.yap.engine.ecs.WindowResizeEvent
import de.yap.engine.ecs.entities.DynamicEntity
import de.yap.engine.ecs.entities.DynamicEntityType
import de.yap.engine.ecs.entities.MeshAtlas
import de.yap.engine.ecs.entities.PlayerEntity
import de.yap.engine.ecs.systems.CameraSystem
import de.yap.engine.ecs.systems.LevelEditor
import de.yap.engine.ecs.systems.MeshSystem
import de.yap.engine.ecs.systems.PathFindingSystem
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Window
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
    lateinit var cameraSystem: CameraSystem
    val meshAtlas = MeshAtlas()

    var view: Matrix4f = Matrix4f()
    var projection: Matrix4f = Matrix4f()

    val debugMemory = DebugMemory()
    val debugCPU = DebugCPU()
    val debugFrameTiming = DebugFrameTiming()
    val debugFontTexture = DebugFontTexture()
    val debugBoundingBox = DebugBoundingBox()
    private val debugInterface = DebugInterface()

    override fun init(window: Window) {
        this.window = window

        renderer.init()
        fontRenderer.init()
        meshAtlas.init()

        initSystems()

        initEntities()

        // we need to fire this ourselves, because we are not registered to listen to events at the time when window actually fires this
        entityManager.fireEvent(WindowResizeEvent(window.width, window.height))
    }

    private fun initSystems() {
        entityManager.registerEventListener(this)
        entityManager.registerSystem(LevelEditor())
        cameraSystem = CameraSystem()
        entityManager.registerSystem(cameraSystem)
        entityManager.registerSystem(MeshSystem())
        entityManager.registerSystem(PathFindingSystem())
        initDebugSystems()
        entityManager.init()
    }

    private fun initDebugSystems() {
        entityManager.registerSystem(debugMemory)
        entityManager.registerSystem(debugCPU)
        entityManager.registerSystem(debugFrameTiming)
        entityManager.registerSystem(debugFontTexture)
        entityManager.registerSystem(debugBoundingBox)
        entityManager.registerSystem(debugInterface)
    }

    private fun initEntities() {
        entityManager.addEntity(PlayerEntity(Vector3f(5.0F, 5.0F, 5.0F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F), hasInput = true))
        entityManager.addEntity(PlayerEntity(Vector3f(2.0F, 2.0F, 2.0F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F), hasInput = false))

        val position = Vector3f(5.0F, 0.0F, 5.0F)
        val goal = Vector3f(0.0F, 0.0F, 0.0F)
        entityManager.addEntity(DynamicEntity(DynamicEntityType.SIMPLE_AI, position, goal))

        val levelGenerator = LevelGenerator()
        for (entity in levelGenerator.generateLevelEntities()) {
            entityManager.addEntity(entity)
        }
    }

    @Subscribe
    fun keyboardEvent(event: KeyboardEvent) {
        fun keyReleased(key: Int): Boolean {
            return event.key == key && event.action == GLFW.GLFW_RELEASE
        }

        if (keyReleased(GLFW.GLFW_KEY_ESCAPE)) {
            window.close()
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
}
