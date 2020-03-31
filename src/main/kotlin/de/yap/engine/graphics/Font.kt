package de.yap.engine.graphics

import de.yap.engine.mesh.Material
import de.yap.engine.util.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class Font(
        val material: Material,
        val fontHeight: Float,
        val bitmapWidth: Int, val bitmapHeight: Int,
        private val scaleForPixelHeight: Float,
        private val ascent: Int, private val descent: Int, private val lineGap: Int,
        private val firstChar: Int, private val cdata: STBTTBakedChar.Buffer
) {

    companion object {
        private val log: Logger = LogManager.getLogger()

        fun fromInternalFile(file: String): Font {
            val ttf = IOUtils.loadInternalResource(file)
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
            var bitmap: ByteBuffer? = null
            try {
                bitmap = MemoryUtil.memAlloc(bitmapWidth * bitmapHeight)
                checkNotNull(bitmap) { "Failed to initialize bitmap for font $file." }

                val firstChar = 32
                STBTruetype.stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, bitmapWidth, bitmapHeight, firstChar, cdata)

                val scaleForPixelHeight = STBTruetype.stbtt_ScaleForPixelHeight(info, fontHeight)

                val material = Material("Font")
                val texture = Texture(bitmapWidth, bitmapHeight, bitmap, GL11.GL_ALPHA)
                texture.bind() // binding the texture once, so that it is uploaded to the GPU
                material.texture = texture
                return Font(
                        material,
                        fontHeight,
                        bitmapWidth, bitmapHeight,
                        scaleForPixelHeight,
                        ascent, descent, lineGap,
                        firstChar, cdata
                )
            } finally {
                MemoryUtil.memFree(bitmap)
            }
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