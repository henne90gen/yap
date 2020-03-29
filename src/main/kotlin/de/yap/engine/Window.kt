package de.yap.engine

import de.yap.engine.ecs.KeyboardEvent
import de.yap.engine.ecs.MouseClickEvent
import de.yap.engine.ecs.MouseMoveEvent
import de.yap.engine.ecs.WindowResizeEvent
import de.yap.game.YapGame
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil


class Window(private val title: String, var width: Int, var height: Int, private var vSync: Boolean) {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private var windowHandle: Long = 0
    private var debugCallback: Callback? = null
    private var ups = 0.0F
    private var fps = 0.0F

    fun setMousePosition(x: Double, y: Double) {
        var xPos = x             // (-aspectRatio, aspectRatio) (left, right)
        xPos *= aspectRatio()    // (-1, 1)
        xPos += 1.0F             // (0, 2)
        xPos *= 0.5F             // (0, 1)
        xPos *= width.toFloat()  // (0, width)
        var yPos = y             // (-1, 1) (bottom, top)
        yPos += 1.0F             // (0, 2)
        yPos *= 0.5F             // (0, 1)
        yPos *= -1.0F            // (0, -1)
        yPos += 1.0F             // (1, 0)
        yPos *= height.toFloat() // (height, 0)
        glfwSetCursorPos(windowHandle, xPos.toDouble(), yPos.toDouble())
    }

    fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL11.GL_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle) { _: Long, width: Int, height: Int ->
            this.width = width
            this.height = height
            YapGame.getInstance().entityManager.fireEvent(WindowResizeEvent(width, height))
        }

        // Setup a key callback
        glfwSetKeyCallback(windowHandle) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            YapGame.getInstance().entityManager.fireEvent(KeyboardEvent(key, scancode, action, mods))
        }

        // Setup the mouse
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        glfwSetCursorPosCallback(windowHandle) { _: Long, xpos: Double, ypos: Double ->
            fireMouseMoveEvent(xpos.toFloat(), ypos.toFloat())
        }

        glfwSetMouseButtonCallback(windowHandle) { _: Long, button: Int, action: Int, mods: Int ->
            YapGame.getInstance().entityManager.fireEvent(MouseClickEvent(button, action, mods))
        }

        // Get the resolution of the primary monitor
        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        // Center our window
        glfwSetWindowPos(
                windowHandle,
                (videoMode!!.width() - width) / 2,
                (videoMode.height() - height) / 2
        )

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle)
        setVSync(vSync)

        // Make the window visible
        glfwShowWindow(windowHandle)
        GL.createCapabilities()

        debugCallback = GLUtil.setupDebugMessageCallback() // may return null if the debug mode is not available

        setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    fun cleanUp() {
        debugCallback?.free()
    }

    /**
     * Updates the mouse position with the given coordinates in pixel coordinates.
     * The final mouse position is in OpenGL screen space coordinates.
     *
     * (-aspectRatio,1)          (aspectRatio,1)
     *         +----------------------+
     *         |                      |
     *         |                      |
     *         |                      |
     *         |                      |
     *         +----------------------+
     * (-aspectRatio,-1)         (aspectRatio,-1)
     */
    private fun fireMouseMoveEvent(xPos: Float, yPos: Float) {
        var x = xPos * (1.0 / width)
        var y = yPos * (1.0 / height)

        // flip y axis (currently increases from top to bottom)
        y -= 1.0F
        y *= -1.0F

        // adjust coordinate system to go from -1 to 1 in x and y
        x *= 2.0
        y *= 2.0
        x -= 1.0
        y -= 1.0

        // scale x to match the aspect ratio of the window
        x *= aspectRatio()

        // fire event
        YapGame.getInstance().entityManager.fireEvent(MouseMoveEvent(x.toFloat(), y.toFloat()))
    }

    fun setClearColor(r: Float, g: Float, b: Float, alpha: Float) {
        GL11.glClearColor(r, g, b, alpha)
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS
    }

    fun windowShouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }

    fun isVSync(): Boolean {
        return vSync
    }

    fun setVSync(vSync: Boolean) {
        this.vSync = vSync
        if (vSync) {
            // Enable v-sync
            glfwSwapInterval(1)
        } else {
            // Disable v-sync
            glfwSwapInterval(0)
        }
    }

    fun render() {
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()
    }

    fun setUps(ups: Float) {
        this.ups = ups
        updateWindowTitle()
    }

    fun setFps(fps: Float) {
        this.fps = fps
        updateWindowTitle()
    }

    private fun updateWindowTitle() {
        val formattedFps = "%.4f".format(fps)
        val formattedUps = "%.4f".format(ups)
        glfwSetWindowTitle(windowHandle, "$title (fps: $formattedFps, ups: $formattedUps)")
    }

    fun aspectRatio(): Float {
        return width.toFloat() / height.toFloat()
    }

    fun close() {
        glfwSetWindowShouldClose(windowHandle, true)
    }
}
