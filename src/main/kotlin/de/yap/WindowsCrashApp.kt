package de.yap

import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil
import java.awt.SystemColor.window


class WindowsCrashApp {

    private var windowHandle: Long = 0
    private var width = 600
    private var height = 400
    private val title = "MyWindow"

    private var debugCallback: Callback? = null

    fun run() {
        setup()
        loop()
        cleanUp()
    }

    private fun loop() {
        val renderer = Renderer()
        // TODO replace this with all the code that gets actually run here
        renderer.init()

        while (!glfwWindowShouldClose(windowHandle)) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // TODO replace this with all the code that gets actually run here
            renderer.cube()

            glfwSwapBuffers(windowHandle)
            glfwPollEvents()
        }
    }

    private fun setup() {
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
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE)

        // Create the window
        windowHandle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Setup resize callback
        GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _: Long, width: Int, height: Int ->
            this.width = width
            this.height = height
        }

        // Setup a key callback
        GLFW.glfwSetKeyCallback(windowHandle) { window: Long, key: Int, _: Int, action: Int, _: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            }
        }

        // Setup the mouse
        //        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)
        //        GLFW.glfwSetCursorPosCallback(windowHandle) { _: Long, xpos: Double, ypos: Double ->
        //            updateMousePosition(xpos.toFloat(), ypos.toFloat())
        //        }

        // Get the resolution of the primary monitor
        val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        // Center our window
        GLFW.glfwSetWindowPos(
                windowHandle,
                (videoMode!!.width() - width) / 2,
                (videoMode.height() - height) / 2
        )

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(windowHandle)
        setVSync(true)

        // Make the window visible
        GLFW.glfwShowWindow(windowHandle)
        GL.createCapabilities()

        debugCallback = GLUtil.setupDebugMessageCallback() // may return null if the debug mode is not available

        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F)
    }

    private fun cleanUp() {
        debugCallback?.free()
    }

    fun setVSync(vSync: Boolean) {
        if (vSync) {
            // Enable v-sync
            GLFW.glfwSwapInterval(1)
        } else {
            // Disable v-sync
            GLFW.glfwSwapInterval(0)
        }
    }
}

fun main() {
    WindowsCrashApp().run()
}
