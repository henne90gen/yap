package de.yap.game

import de.yap.engine.Camera
import de.yap.engine.IGameLogic
import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.engine.mesh.Mesh
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

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")
    private val fontShader = Shader("src/main/glsl/font_vertex.glsl", "src/main/glsl/font_fragment.glsl")

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

    private var cubeMesh: List<Mesh> = emptyList()

    override fun init() {
        renderer.init()
        shader.compile()
        fontShader.compile()

        cubeMesh = Mesh.fromFile("src/test/resources/cube.obj")
        roomMeshes = Mesh.fromFile("src/main/resources/textures/scene.obj")
    }

    /**
     * Controls:
     *  - W,A,S,D - move in the x-z-plane
     *  - Q,E - move along the y-axis
     *  - SPACE - teleport to point of intersection
     */
    override fun input(window: Window) {
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

        // TODO this is not good enough (boolean switches back and forth really fast)
        if (window.isKeyPressed(GLFW.GLFW_KEY_F)) {
            roomWireframe = !roomWireframe
        }

        // TODO this should only teleport as soon as the player lets go of the teleport button
        if (window.isMousePressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) && cameraRayResult.hasValue()) {
            val point = cameraRayResult.point
            point.add(
                    Vector3f(cameraRayResult.normal)
                            .normalize()
                            .mul(0.5F)
            )
            camera.teleport(point, cameraRayResult.normal)
        }

        // TODO this should only happen once when the key has been released
        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            switchToNextCamera(window)
        }
    }

    override fun update(interval: Float) {
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

    override fun render(window: Window) {
        renderer.clear()
        handleWindowResize(window)

        shader.apply(currentCamera())
        shader.setUniform("color", Vector4f(1.0F))
        fontShader.setUniform("color", Vector4f(1.0F))

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
        renderCameras()
        renderText()
    }

    private fun handleWindowResize(window: Window) {
        if (!window.hasResized) {
            return
        }

        glViewport(0, 0, window.width, window.height)
        window.hasResized = false
        val aspectRatio = window.width.toFloat() / window.height.toFloat()
        currentCamera().aspectRatioChanged(aspectRatio)

        fontShader.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))
    }

    private fun renderText() {
        val color = Vector4f(0.5F, 0.75F, 0.0F, 1.0F)
        for (i in 0..9) {
            for (j in 0..9) {
                val offset = Vector3f(-1.5F, -0.8F, 0.0f)
                        .add(0.3F * j, 0.15F * i, 0.0F)
                renderer.text(fontShader, "${i}x$j", Matrix4f().translate(offset), color)
            }
        }
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
