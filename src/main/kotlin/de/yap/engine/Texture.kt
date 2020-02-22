package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer

data class Texture(val filePath: String) {

    companion object {
        private val log: Logger = LogManager.getLogger(Texture::class.java.name)
    }

    private var id = -1

    init {
        id = load()
    }

    private fun load(): Int {
        var width = 0
        var height = 0
        var buf: ByteBuffer? = null
        // Load Texture file
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            val file = File(filePath)
            if (!file.exists()) {
                log.warn("File '{}' does not exist", filePath)
                throw Exception("Image file [" + filePath + "] not loaded: " + STBImage.stbi_failure_reason())
            }
            val filePath = file.absolutePath

            buf = STBImage.stbi_load(filePath, w, h, channels, 4)
            if (buf == null) {
                throw Exception("Image file [" + filePath + "] not loaded: " + STBImage.stbi_failure_reason())
            }
            width = w.get()
            height = h.get()
        }

        val textureId = GL11.glGenTextures()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)

        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        STBImage.stbi_image_free(buf!!)

        return textureId
    }

    fun getId(): Int {
        return id
    }

}