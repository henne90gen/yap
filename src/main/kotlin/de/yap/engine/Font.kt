package de.yap.engine

import de.yap.engine.mesh.Material
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.file.Files

class Font(
        val material: Material,
        val fontHeight: Float,
        val bitmapWidth: Int, val bitmapHeight: Int,
        private val scaleForPixelHeight: Float,
        private val ascent: Int, private val descent: Int, private val lineGap: Int,
        private val firstChar: Int, private val cdata: STBTTBakedChar.Buffer
) {

    companion object {
        fun fromFile(file: File): Font {
            val ttf = loadResource(file)
            checkNotNull(ttf) { "Could not load font!" }

            val info: STBTTFontinfo = STBTTFontinfo.create()
            check(STBTruetype.stbtt_InitFont(info, ttf)) { "Failed to initialize font information." }

            var ascent = 0
            var descent = 0
            var lineGap = 0

            MemoryStack.stackPush().use { stack ->
                val pAscent = stack.mallocInt(1)
                val pDescent = stack.mallocInt(1)
                val pLineGap = stack.mallocInt(1)

                STBTruetype.stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)

                ascent = pAscent.get(0)
                descent = pDescent.get(0)
                lineGap = pLineGap.get(0)
            }

            val cdata = STBTTBakedChar.malloc(96)

            val fontHeight = 100.0F
            val bitmapWidth = 512
            val bitmapHeight = 512
            val bitmap = BufferUtils.createByteBuffer((bitmapWidth * bitmapHeight))
            val firstChar = 32
            STBTruetype.stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, bitmapWidth, bitmapHeight, firstChar, cdata)

            GL13.glEnable(GL13.GL_TEXTURE_2D)
            GL13.glEnable(GL13.GL_BLEND)
            GL13.glBlendFunc(GL13.GL_SRC_ALPHA, GL13.GL_ONE_MINUS_SRC_ALPHA)

            val scaleForPixelHeight = STBTruetype.stbtt_ScaleForPixelHeight(info, fontHeight)

            val material = Material("Font")
            material.texture = Texture(bitmapWidth, bitmapHeight, bitmap, GL11.GL_ALPHA)
            return Font(
                    material,
                    fontHeight,
                    bitmapWidth, bitmapHeight,
                    scaleForPixelHeight,
                    ascent, descent, lineGap,
                    firstChar, cdata
            )
        }

        private fun loadResource(file: File): ByteBuffer? {
            var buffer: ByteBuffer? = null

            val path = file.toPath()
            if (!Files.isReadable(path)) {
                return null
            }

            buffer = Files.newByteChannel(path).use { fc ->
                val buf = BufferUtils.createByteBuffer(fc.size().toInt() + 1)
                while (fc.read(buf) != -1) {
                }
                buf
            }

            if (buffer == null) {
                return null
            }

            buffer.flip()
            return buffer.slice()
        }
    }

    fun lineOffset(): Float {
        val lineSpacing = 0.75F // This looks pretty good
        return (ascent - descent + lineGap) * scaleForPixelHeight * lineSpacing
    }

    fun getBakedQuad(codePoint: Int, xPosBuf: FloatBuffer, yPosBuf: FloatBuffer, quad: STBTTAlignedQuad) {
        STBTruetype.stbtt_GetBakedQuad(
                cdata,
                bitmapWidth, bitmapHeight,
                codePoint - firstChar,
                xPosBuf, yPosBuf, quad,
                true
        )
    }
}