package de.yap.game

import org.lwjgl.opengl.GL11

class Renderer {
    @Throws(Exception::class)
    fun init() {
    }

    fun clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }
}
