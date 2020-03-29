package de.yap.game

import de.yap.engine.Camera
import de.yap.engine.IGameLogic
import de.yap.engine.Window
import de.yap.engine.debug.DebugFontTexture
import de.yap.engine.debug.DebugInterface
import de.yap.engine.ecs.*
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Text
import de.yap.engine.mesh.Mesh
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame private constructor() : IGameLogic {

    companion object {
        private val log: Logger = LogManager.getLogger(YapGame::class.java.name)

        private val yapGame: YapGame = YapGame()

        fun getInstance(): YapGame {
            return yapGame
        }
    }

    lateinit var window: Window

    val entityManager = EntityManager()

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    val renderer = Renderer()
    val fontRenderer = FontRenderer()

    private val camera = Camera(Vector3f(0.5F, 0.0F, 3.0F))
    private val secondCamera = Camera()
    private var selectedCamera = 0

    private var cameraRayStart = Vector3f()
    private var cameraRayResult = IntersectionResult()

    private var roomWireframe = false

    private var mousePosition = Vector2f()

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

        entityManager.registerEventListener(this)
        entityManager.registerSystem(DebugFontTexture())
        entityManager.registerSystem(DebugInterface())

        entityManager.init()

        // we need to fire this ourselves, because we are not registered to the event bus at the time when window would actually fire this
        entityManager.fireEvent(WindowResizeEvent(window.width, window.height))
    }

    private fun createText(): Text {
        var value = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vitae purus dolor. Mauris pellentesque commodo nulla, sit amet euismod sapien viverra ut. Cras commodo euismod turpis, ac lobortis augue. Nam consequat sodales quam ac porttitor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc non est iaculis, posuere diam a, suscipit nibh. Fusce nec erat vel sapien dictum pulvinar eu porttitor leo. Nulla finibus dolor turpis, eu sagittis risus tincidunt sed. Ut convallis augue massa, vel dapibus mauris scelerisque eget. Duis sollicitudin vulputate augue, tincidunt ornare dolor feugiat at."
        value = value.replace(". ", ".\n")
        val color = Vector4f(0.0F, 0.5F, 0.75F, 1.0F)
        val transform = Matrix4f().translate(-2.0F, 2.0F, 0.0F)
        return Text(value, fontRenderer.font, transform, color)
    }

    @Subscribe
    fun mouseCallback(event: MouseClickEvent) {
        // TODO refactor this into a system
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == GLFW.GLFW_RELEASE && cameraRayResult.hasValue()) {
            val point = cameraRayResult.point
            point.add(
                    Vector3f(cameraRayResult.normal)
                            .normalize()
                            .mul(0.5F)
            )
            camera.teleport(point, cameraRayResult.normal)
        }
    }

    /**
     * Controls:
     *  - W,A,S,D - move in the x-z-plane
     *  - Q,E - move along the y-axis
     *  - Left Mouse Click - teleport to point of intersection
     *  - SPACE - change camera perspective
     */
    @Subscribe
    fun keyboardEvent(event: KeyboardEvent) {
        fun keyPressed(key: Int): Boolean {
            return event.key == key && (event.action == GLFW.GLFW_PRESS || event.action == GLFW.GLFW_REPEAT)
        }

        fun keyReleased(key: Int): Boolean {
            return event.key == key && event.action == GLFW.GLFW_RELEASE
        }

        // TODO for movement it is probably better to poll the current state of these keys instead of listening for events
        //      -> move this into update() and ask the window about the current state of these keys
        direction.x = when {
            keyPressed(GLFW.GLFW_KEY_D) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_A) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_A) || keyReleased(GLFW.GLFW_KEY_D) -> {
                0.0F
            }
            else -> {
                direction.x
            }
        }
        direction.y = when {
            keyPressed(GLFW.GLFW_KEY_Q) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_E) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_Q) || keyReleased(GLFW.GLFW_KEY_E) -> {
                0.0F
            }
            else -> {
                direction.y
            }
        }
        direction.z = when {
            keyPressed(GLFW.GLFW_KEY_S) -> {
                1.0F
            }
            keyPressed(GLFW.GLFW_KEY_W) -> {
                -1.0F
            }
            keyReleased(GLFW.GLFW_KEY_S) || keyReleased(GLFW.GLFW_KEY_W) -> {
                0.0F
            }
            else -> {
                direction.z
            }
        }

        if (keyReleased(GLFW.GLFW_KEY_ESCAPE)) {
            window.close()
        }
        if (keyReleased(GLFW.GLFW_KEY_F)) {
            roomWireframe = !roomWireframe
        }
        if (keyReleased(GLFW.GLFW_KEY_SPACE)) {
            switchToNextCamera(window)
        }
    }

    @Subscribe
    fun mouseEvent(event: MouseMoveEvent) {
        mousePosition = Vector2f(event.x, event.y)
        window.setMousePosition(0.0, 0.0)
    }

    override fun update(interval: Float) {
        currentCamera().update(direction, mousePosition)
        mousePosition = Vector2f(0.0F)

        val cameraDirection = camera.direction()
        val startOffset = Vector3f(cameraDirection)
                .add(0.0F, -0.1F, 0.0F) // move the start down a little
                .normalize()
                .mul(0.01F)
        cameraRayStart = Vector3f(camera.position)
                .add(startOffset)

        cameraRayResult = intersects(cameraRayStart, cameraDirection, roomMeshes, roomTransformation)

        entityManager.update(interval)
    }

    override fun render() {
        renderer.clear()

        renderer.shader3D.apply(currentCamera())
        renderer.shader3D.setUniform("color", Vector4f(1.0F))
        renderer.shader3D.setUniform("lightPos", Vector3f(2.0f, 0.0f, 4.0f))
        renderer.shader3D.setUniform("lightColor", Vector3f(0.5f, 0.3f, 0.2f))

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
        renderCameras()
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
    fun handleWindowResize(event: WindowResizeEvent) {
        glViewport(0, 0, window.width, window.height)
        currentCamera().aspectRatioChanged(window.aspectRatio())
        fontRenderer.aspectRatio = window.aspectRatio()
    }

    private fun renderTextInScene(text: Text?) {
        if (text == null) {
            return
        }

        fontRenderer.stringInScene(text, currentCamera())
    }

    private fun renderCameras() {
        // TODO show "up" axis of camera
        if (selectedCamera == 0) {
            renderer.cube(Matrix4f().translate(secondCamera.position).scale(0.4F), Vector4f(1.0F, 1.0F, 0.0F, 1.0F))
        } else {
            renderer.cube(Matrix4f().translate(camera.position).scale(0.4F), Vector4f(1.0F, 0.0F, 1.0F, 1.0F))
        }
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

    private fun renderRayFromCamera() {
        val color = Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
        if (cameraRayResult.hasValue()) {
            renderer.line(cameraRayStart, cameraRayResult.point, color)
            renderer.cube(Matrix4f().translate(cameraRayResult.point).scale(0.1F), color)
        } else {
            renderer.line(cameraRayStart, camera.direction().mul(1000.0F), color)
        }
    }

    private fun currentCamera(): Camera {
        return if (selectedCamera == 0) {
            camera
        } else {
            secondCamera
        }
    }

    private fun switchToNextCamera(window: Window) {
        selectedCamera++
        selectedCamera %= 2
        val aspectRatio = window.width.toFloat() / window.height.toFloat()
        currentCamera().aspectRatioChanged(aspectRatio)
    }
}
