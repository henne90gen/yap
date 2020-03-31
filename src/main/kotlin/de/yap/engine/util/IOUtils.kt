package de.yap.engine.util

import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

class IOUtils {
    companion object {
        private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
            val newBuffer = BufferUtils.createByteBuffer(newCapacity)
            buffer.flip()
            newBuffer.put(buffer)
            return newBuffer
        }

        fun loadInternalResource(resource: String): ByteBuffer? {
            val source: InputStream? = IOUtils::class.java.classLoader.getResourceAsStream(resource)
            checkNotNull(source) { "Could not load internal resource '$resource'" }

            // TODO decide on a default buffer size
            val bufferSize = 1024
            val buffer = source.use {
                val rbc: ReadableByteChannel = Channels.newChannel(it)
                rbc.use {
                    var buf = BufferUtils.createByteBuffer(bufferSize)

                    while (true) {
                        val bytes = rbc.read(buf)
                        if (bytes == -1) {
                            break
                        }
                        if (buf.remaining() == 0) {
                            buf = resizeBuffer(buf, buf.capacity() * 3 / 2) // 50%
                        }
                    }
                    buf
                }
            }

            buffer.flip()
            return buffer.slice()
        }

        fun loadInternalTextFile(filePath: String): List<String> {
            val source: InputStream? = IOUtils::class.java.classLoader.getResourceAsStream(filePath)
            checkNotNull(source) { "Could not load internal text file '$filePath'" }

            return source.bufferedReader().lines().toList()
        }

        fun loadExternalResource(resource: String): ByteBuffer? {
            val path = Paths.get(resource)
            if (!Files.isReadable(path)) {
                return null
            }

            val buffer = Files.newByteChannel(path).use { fc ->
                val buf = BufferUtils.createByteBuffer(fc.size().toInt() + 1)
                while (fc.read(buf) != -1) {
                }
                buf
            }

            buffer.flip()
            return buffer.slice()
        }
    }
}
