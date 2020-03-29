package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer

class Texture(
        private val width: Int,
        private val height: Int,
        private val data: ByteBuffer,
        private val format: Int
) {

    companion object {
        private val log: Logger = LogManager.getLogger()

        fun fromFile(file: File): Texture {
            check(file.exists()) { "File '$file' does not exist" }
            val filePath = file.absolutePath

            var buf: ByteBuffer? = null
            var width = 0
            var height = 0
            var channels = 0
            MemoryStack.stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val c = stack.mallocInt(1)

                buf = STBImage.stbi_load(filePath, w, h, c, 4)
                checkNotNull(buf) { "Image file [" + file + "] not loaded: " + STBImage.stbi_failure_reason() }
                width = w.get()
                height = h.get()
                channels = c.get()
            }
            log.info("{}: {}x{} - {} channels", file, width, height, channels)
            val format = when (channels) {
                4 -> {
                    GL_RGBA
                }
                3 -> {
                    GL_RGBA
                }
                1 -> {
                    GL_ALPHA
                }
                else -> {
                    GL_RGBA
                }
            }
            return Texture(width, height, buf!!, format)
        }
    }

    private var id: Int? = null

    private fun load(): Int {
        val textureId = glGenTextures()

        glActiveTexture(GL30.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, textureId)
        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width, height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                data
        )

        GL30.glGenerateMipmap(GL_TEXTURE_2D)

        return textureId
    }

    fun bind() {
        if (id == null) {
            id = load()
        }

        glActiveTexture(GL13.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, id!!)
    }
}