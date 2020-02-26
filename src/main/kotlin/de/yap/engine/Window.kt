package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil

class Window(private val title: String, var width: Int, var height: Int, private var vSync: Boolean) {

    companion object {
        private val log: Logger = LogManager.getLogger(Window::class.java.name)
    }

    private var windowHandle: Long = 0
    private var ups = 0.0F
    private var fps = 0.0F

    var mousePosition = Vector2f(0.0F)
        set(value) {
//            log.info("Setting MousePosition: {}", value)
            var xPos = value.x       // (-aspectRatio, aspectRatio) (left, right)
            xPos *= aspectRatio()      // (-1, 1)
            xPos += 1.0F             // (0, 2)
            xPos *= 0.5F             // (0, 1)
            xPos *= width.toFloat()  // (0, width)
            var yPos = value.y       // (-1, 1) (bottom, top)
            yPos += 1.0F             // (0, 2)
            yPos *= 0.5F             // (0, 1)
            yPos *= -1.0F            // (0, -1)
            yPos += 1.0F             // (1, 0)
            yPos *= height.toFloat() // (height, 0)
            GLFW.glfwSetCursorPos(windowHandle, xPos.toDouble(), yPos.toDouble())
            field = value
        }
        get() {
//            log.info("Getting MousePosition: {}", field)
            return field
        }
    var hasResized = true

    fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)

        // Create the window
        windowHandle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup resize callback
        GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _: Long, width: Int, height: Int ->
            this.width = width
            this.height = height
            hasResized = true
        }

        // Setup a key callback
        GLFW.glfwSetKeyCallback(windowHandle) { window: Long, key: Int, _: Int, action: Int, _: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            }
        }

        // Setup the mouse
        mousePosition = Vector2f(0.0F)
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwSetCursorPosCallback(windowHandle) { _: Long, xpos: Double, ypos: Double ->
            updateMousePosition(xpos.toFloat(), ypos.toFloat())
        }

        // Get the resolution of the primary monitor
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        // Center our window
        GLFW.glfwSetWindowPos(
                windowHandle,
                (vidmode!!.width() - width) / 2,
                (vidmode.height() - height) / 2
        )

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(windowHandle)
        setVSync(vSync)

        // Make the window visible
        GLFW.glfwShowWindow(windowHandle)
        GL.createCapabilities()

        setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
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
    private fun updateMousePosition(xPos: Float, yPos: Float) {
        mousePosition.x = xPos * (1.0F / width)
        mousePosition.y = yPos * (1.0F / height)

        // flip y axis (currently increases from top to bottom)
        mousePosition.y -= 1.0F
        mousePosition.y *= -1.0F

        // adjust coordinate system to go from -1 to 1 in x and y
        mousePosition
                .mul(2.0F)
                .sub(Vector2f(1.0F))

        // scale x to match the aspect ratio of the window
        mousePosition.x *= aspectRatio()
    }

    fun setClearColor(r: Float, g: Float, b: Float, alpha: Float) {
        GL11.glClearColor(r, g, b, alpha)
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS
    }

    fun isMousePressed(mouseButtonCode: Int): Boolean {
        return GLFW.glfwGetMouseButton(windowHandle, mouseButtonCode) == GLFW.GLFW_PRESS
    }

    fun windowShouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(windowHandle)
    }

    fun isVSync(): Boolean {
        return vSync
    }

    fun setVSync(vSync: Boolean) {
        this.vSync = vSync
        if (vSync) {
            // Enable v-sync
            GLFW.glfwSwapInterval(1)
        } else {
            // Disable v-sync
            GLFW.glfwSwapInterval(0)
        }
    }

    fun update() {
        GLFW.glfwSwapBuffers(windowHandle)
        GLFW.glfwPollEvents()
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
        GLFW.glfwSetWindowTitle(windowHandle, "$title (fps: $formattedFps, ups: $formattedUps)")
    }

    fun aspectRatio(): Float {
        return width.toFloat() / height.toFloat()
    }
}
