package de.yap.game

import de.yap.engine.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL20.glViewport


class YapGame : IGameLogic {

    private val log: Logger = LogManager.getLogger(this.javaClass.name)

    private val direction = Vector3f(0.0f, 0.0f, 0.0f)
    private val renderer: Renderer = Renderer()
    private val shader = Shader("src/main/glsl/vertex.glsl", "src/main/glsl/fragment.glsl")
    private val camera = Camera(Vector3f(0.5F, 0.0F, 3.0F), Matrix4f())
    private val cameraSpeed = 0.1F
    private var cameraRayStart = Vector3f()
    private var cameraRayResult = IntersectionResult()
    private var roomWireframe = false

    private lateinit var roomMesh: Mesh
    private val scale = 2.0F
    private val negativeScaleHalf = -0.5F * scale
    private val position = Vector3f(negativeScaleHalf)
    private val roomTransformation = Matrix4f().translate(position).scale(scale)


    override fun init() {
        renderer.init()
        shader.compile()

        roomMesh = Mesh()
                // back
                .withQuad(
                        Vector3f(0.0F, 1.0F, 0.0F),
                        Vector3f(0.0F, 0.0F, 0.0F),
                        Vector3f(1.0F, 1.0F, 0.0F)
                )
                // front
                .withQuad(
                        Vector3f(1.0F, 1.0F, 1.0F),
                        Vector3f(1.0F, 0.0F, 1.0F),
                        Vector3f(0.0F, 1.0F, 1.0F)
                )
                // left
                .withQuad(
                        Vector3f(0.0F, 1.0F, 1.0F),
                        Vector3f(0.0F, 0.0F, 1.0F),
                        Vector3f(0.0F, 1.0F, 0.0F)
                )
                // right
                .withQuad(
                        Vector3f(1.0F, 1.0F, 0.0F),
                        Vector3f(1.0F, 0.0F, 0.0F),
                        Vector3f(1.0F, 1.0F, 1.0F)
                )
                // top
                .withQuad(
                        Vector3f(0.0F, 1.0F, 1.0F),
                        Vector3f(0.0F, 1.0F, 0.0F),
                        Vector3f(1.0F, 1.0F, 1.0F)
                )
                // bottom
                .withQuad(
                        Vector3f(0.0F, 0.0F, 0.0F),
                        Vector3f(0.0F, 0.0F, 1.0F),
                        Vector3f(1.0F, 0.0F, 0.0F)
                )
    }

    /**
     * Controls:
     *  - W,A,S,D - move in the x-z-plane
     *  - q,E - move along the y-axis
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

        // TODO this is not good enough (boolean switches back and forth really fast)
        if (window.isKeyPressed(GLFW.GLFW_KEY_F)) {
            roomWireframe = !roomWireframe
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_SPACE) && cameraRayResult.hasValue()) {
            camera.teleport(cameraRayResult.point)
        }
    }

    override fun update(interval: Float) {
        val tmp = Vector3f(direction).mul(cameraSpeed)
        camera.move(tmp)

        val dir = Vector4f(0.0F, 0.0F, -1.0F, 0.0F)
                .mul(camera.rotation)
        val cameraDirection = Vector3f(dir.x, dir.y, dir.z)

        val startOffset = Vector3f(cameraDirection)
                .add(0.0F, -0.1F, 0.0F) // move the start down a little
                .normalize()
                .mul(0.01F)
        cameraRayStart = Vector3f(camera.position)
                .add(startOffset)

        cameraRayResult = intersects(cameraRayStart, cameraDirection, roomMesh, roomTransformation)
    }

    override fun render(window: Window) {
        renderer.clear()
        if (window.isResized) {
            glViewport(0, 0, window.width, window.height)
            window.isResized = false
            val aspectRatio = window.width.toFloat() / window.height.toFloat()
            camera.aspectRatioChanged(aspectRatio)
        }

        shader.apply(camera)
        shader.setUniform("color", Vector4f(1.0F, 1.0F, 1.0F, 1.0F))

        renderRayFromCamera()
        renderCoordinateSystemAxis()
        renderRoom()
    }

    private fun renderRoom() {
        renderer.wireframe(roomWireframe) {
            renderer.mesh(shader, roomMesh, roomTransformation)
        }
    }

    private fun renderCoordinateSystemAxis() {
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(1.0F, 0.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 1.0F, 0.0F)).scale(0.1F))
        renderer.cube(shader, Matrix4f().translate(Vector3f(0.0F, 0.0F, 1.0F)).scale(0.1F))

        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 1.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 0.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F))

        renderer.line(shader, Vector3f(0.0F, 0.0F, 1.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(1.0F, 0.0F, 0.0F))
        renderer.line(shader, Vector3f(0.0F, 1.0F, 0.0F), Vector3f(0.0F, 0.0F, 1.0F))
    }

    private fun renderRayFromCamera() {
        shader.setUniform("color", Vector4f(1.0F, 0.0F, 0.0F, 1.0F))
        if (cameraRayResult.hasValue()) {
            renderer.line(shader, cameraRayStart, cameraRayResult.point)
            renderer.cube(shader, Matrix4f().translate(cameraRayResult.point).scale(0.1F))
        }
        shader.setUniform("color", Vector4f(1.0F, 1.0F, 1.0F, 1.0F))
    }
}
