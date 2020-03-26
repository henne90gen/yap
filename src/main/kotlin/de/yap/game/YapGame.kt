package de.yap.game

import de.yap.engine.Camera
import de.yap.engine.IGameLogic
import de.yap.engine.Window
import de.yap.engine.debug.DebugInterface
import de.yap.engine.events.*
import de.yap.engine.graphics.FontRenderer
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Text
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20.glViewport


class YapGame private constructor() : IGameLogic {

    companion object {
        private val log: Logger = LogManager.getLogger(YapGame::class.java.name)

        private val yapGame: YapGame = YapGame()

        fun getInstance(): YapGame {
            return yapGame
        }
    }

    private lateinit var window: Window

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    val renderer = Renderer()
    private val debugInterface = DebugInterface()
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

        debugInterface.init(fontRenderer)

        roomMeshes = Mesh.fromFile("models/scene.obj")

        text = createText()

        window.setKeyCallback(::keyCallback)
        window.setMouseCallback(::mouseCallback)

        EventBus.getInstance().fire(InitEvent())
    }

    private fun createText(): Text {
        var value = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vitae purus dolor. Mauris pellentesque commodo nulla, sit amet euismod sapien viverra ut. Cras commodo euismod turpis, ac lobortis augue. Nam consequat sodales quam ac porttitor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc non est iaculis, posuere diam a, suscipit nibh. Fusce nec erat vel sapien dictum pulvinar eu porttitor leo. Nulla finibus dolor turpis, eu sagittis risus tincidunt sed. Ut convallis augue massa, vel dapibus mauris scelerisque eget. Duis sollicitudin vulputate augue, tincidunt ornare dolor feugiat at."
        value = value.replace(". ", ".\n")
        val color = Vector4f(0.0F, 0.5F, 0.75F, 1.0F)
        val transform = Matrix4f().translate(-2.0F, 2.0F, 0.0F)
        return Text(value, fontRenderer.font, transform, color)
    }

    private fun keyCallback(windowHandle: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
            GLFW.glfwSetWindowShouldClose(windowHandle, true) // We will detect this in the rendering loop
        }

        if (key == GLFW.GLFW_KEY_F && action == GLFW.GLFW_RELEASE) {
            roomWireframe = !roomWireframe
        }

        if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_RELEASE) {
            switchToNextCamera(window)
        }

        debugInterface.keyCallback(windowHandle, key, scancode, action, mods)
    }

    private fun mouseCallback(windowHandle: Long, button: Int, action: Int, mods: Int) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE && cameraRayResult.hasValue()) {
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
    override fun input() {
        debugInterface.input()

        direction.x = when {
            window.isKeyPressed(GLFW.GLFW_KEY_D) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_A) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        direction.y = when {
            window.isKeyPressed(GLFW.GLFW_KEY_Q) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_E) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }
        direction.z = when {
            window.isKeyPressed(GLFW.GLFW_KEY_S) -> {
                1.0F
            }
            window.isKeyPressed(GLFW.GLFW_KEY_W) -> {
                -1.0F
            }
            else -> {
                0.0F
            }
        }

        mousePosition = Vector2f(window.mousePosition)
        window.mousePosition = Vector2f(0.0F)

        EventBus.getInstance().fire(InputEvent())
    }

    override fun update(interval: Float) {
        debugInterface.update(interval)

        currentCamera().update(direction, mousePosition)

        val cameraDirection = camera.direction()
        val startOffset = Vector3f(cameraDirection)
                .add(0.0F, -0.1F, 0.0F) // move the start down a little
                .normalize()
                .mul(0.01F)
        cameraRayStart = Vector3f(camera.position)
                .add(startOffset)

        cameraRayResult = intersects(cameraRayStart, cameraDirection, roomMeshes, roomTransformation)

        EventBus.getInstance().fire(UpdateEvent())
    }

    override fun render() {
        renderer.clear()

        handleWindowResize(window)

        renderer.shader3D.apply(currentCamera())
        renderer.shader3D.setUniform("color", Vector4f(1.0F))
        renderer.shader3D.setUniform("lightPos", Vector3f(2.0f, 0.0f, 4.0f))
        renderer.shader3D.setUniform("lightColor", Vector3f(0.5f, 0.3f, 0.2f))

        renderFontTexture()

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
        renderCameras()
        renderTextInScene(text)
//        renderTextOnScreen(text)

        debugInterface.render(window)

        EventBus.getInstance().fire(RenderEvent())
    }

    private fun renderTextOnScreen(text: Text?) {
        val color = Vector4f(0.5F, 0.75F, 0.0F, 1.0F)
        val transform = Matrix4f().translate(-window.aspectRatio(), 0.85F, 0.0F)
        text?.value?.let { fontRenderer.string(it, transform, color) }
    }

    private fun renderFontTexture() {
        val mesh = MeshUtils.quad2D(material = fontRenderer.font.material)
        fontRenderer.fontShader.bind()
        fontRenderer.fontShader.setUniform("model", Matrix4f())
        fontRenderer.fontShader.setUniform("color", Vector4f(0.5F))

        if (mesh.hasTexture()) {
            fontRenderer.fontShader.setUniform("textureSampler", 1)
        } else {
            fontRenderer.fontShader.setUniform("textureSampler", 0)
        }

        mesh.bind()

        GL13.glDrawElements(GL13.GL_TRIANGLES, mesh.indices.size * 3, GL13.GL_UNSIGNED_INT, 0)

        fontRenderer.fontShader.unbind()
    }

    private fun handleWindowResize(window: Window) {
        if (!window.hasResized) {
            return
        }

        glViewport(0, 0, window.width, window.height)
        window.hasResized = false
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
