package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer

data class Texture(val file: File) {

    companion object {
        private val log: Logger = LogManager.getLogger(Texture::class.java.name)
    }

    private var id: Int? = null
    private var width: Int = 0
    private var height: Int = 0

    private fun load(): Int {
        var buf: ByteBuffer? = null
        // Load Texture file
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            if (!file.exists()) {
                log.warn("File '{}' does not exist", file)
                throw Exception("Image file [" + file + "] not loaded: " + STBImage.stbi_failure_reason())
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

        GL30.glActiveTexture(GL30.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf)

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        STBImage.stbi_image_free(buf!!)

        return textureId
    }

    fun bind() {
        if (id == null) {
            id = load()
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id!!)
    }
}