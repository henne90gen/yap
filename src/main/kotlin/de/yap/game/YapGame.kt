package de.yap.game

import de.yap.engine.Camera
import de.yap.engine.DebugInterface
import de.yap.engine.IGameLogic
import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {

    companion object {
        private val log: Logger = LogManager.getLogger(YapGame::class.java.name)
    }

    private lateinit var window: Window

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    private val renderer = Renderer()
    private val debugInterface = DebugInterface()
    private val shader = Shader("shaders/vertex.glsl", "shaders/fragment.glsl")
    private val fontShader = Shader("shaders/vertex.glsl", "shaders/font_fragment.glsl")

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

    private var textMesh: Mesh? = null

    override fun init(window: Window) {
        this.window = window

        renderer.init()
        debugInterface.init(renderer)
        shader.compile()
        fontShader.compile()

        roomMeshes = Mesh.fromFile("models/scene.obj")

        textMesh = createTextMesh()

        window.setKeyCallback(::keyCallback)
        window.setMouseCallback(::mouseCallback)
    }

    private fun createTextMesh(): Mesh {
        var text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vitae purus dolor. Mauris pellentesque commodo nulla, sit amet euismod sapien viverra ut. Cras commodo euismod turpis, ac lobortis augue. Nam consequat sodales quam ac porttitor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc non est iaculis, posuere diam a, suscipit nibh. Fusce nec erat vel sapien dictum pulvinar eu porttitor leo. Nulla finibus dolor turpis, eu sagittis risus tincidunt sed. Ut convallis augue massa, vel dapibus mauris scelerisque eget. Duis sollicitudin vulputate augue, tincidunt ornare dolor feugiat at."
        text = text.replace(". ", ".\n")
        return MeshUtils.text(renderer.font, text)
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
     *  - SPACE - teleport to point of intersection
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
    }

    override fun render() {
        renderer.clear()

        handleWindowResize(window)

        shader.apply(currentCamera())
        shader.setUniform("color", Vector4f(1.0F))
        shader.setUniform("lightPos", Vector3f(2.0f, 0.0f, 4.0f))
        shader.setUniform("lightColor", Vector3f(0.5f, 0.3f, 0.2f))
        fontShader.setUniform("color", Vector4f(1.0F))

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
        renderCameras()
        renderTextInScene(textMesh)

        debugInterface.render(window, renderer, shader, fontShader)
    }

    private fun handleWindowResize(window: Window) {
        if (!window.hasResized) {
            return
        }

        glViewport(0, 0, window.width, window.height)
        window.hasResized = false
        currentCamera().aspectRatioChanged(window.aspectRatio())
    }

    private fun renderText(window: Window, textMesh: Mesh?) {
        if (textMesh == null) {
            return
        }

        val color = Vector4f(0.5F, 0.75F, 0.0F, 1.0F)
        renderer.uiText(fontShader, window.aspectRatio(), textMesh, Matrix4f().translate(-window.aspectRatio(), 0.85F, 0.0F), color)
    }

    private fun renderTextInScene(textMesh: Mesh?) {
        if (textMesh == null) {
            return
        }

        fontShader.apply(currentCamera())
        val color = Vector4f(0.0F, 0.5F, 0.75F, 1.0F)
        renderer.mesh(fontShader, textMesh, Matrix4f().translate(-2.0F, 2.0F, 0.0F), color)
    }

    private fun renderCameras() {
        // TODO show "up" axis of camera
        if (selectedCamera == 0) {
            renderer.cube(shader, Matrix4f().translate(secondCamera.position).scale(0.4F), Vector4f(1.0F, 1.0F, 0.0F, 1.0F))
        } else {
            renderer.cube(shader, Matrix4f().translate(camera.position).scale(0.4F), Vector4f(1.0F, 0.0F, 1.0F, 1.0F))
        }
    }

    private fun renderRoom() {
        renderer.wireframe(roomWireframe) {
            for (roomMesh in roomMeshes) {
                val color = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
                renderer.mesh(shader, roomMesh, roomTransformation, color)
            }
        }
    }

    private fun renderCoordinateSystemAxis() {
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 0.0F)).scale(0.1F))

        renderer.cube(shader, Matrix4f().translate(Vector3f(1.0F, 0.0F, 0.0F)).scale(0.1F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 1.0F, 0.0F)).scale(0.1F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 1.0F)).scale(0.1F), Vector4f(0.0F, 0.0F, 1.0F, 1.0F))

        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F), Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F), Vector4f(0.0F, 1.0F, 0.0F, 1.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F), Vector4f(0.0F, 0.0F, 1.0F, 1.0F))
    }

    private fun renderRayFromCamera() {
        val color = Vector4f(1.0F, 0.0F, 0.0F, 1.0F)
        if (cameraRayResult.hasValue()) {
            renderer.line(shader, cameraRayStart, cameraRayResult.point, color)
            renderer.cube(shader, Matrix4f().translate(cameraRayResult.point).scale(0.1F), color)
        } else {
            renderer.line(shader, cameraRayStart, camera.direction().mul(1000.0F), color)
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
