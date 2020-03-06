package de.yap.engine.graphics

import de.yap.engine.Font
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.FloatBuffer


class Renderer {

    companion object {
        private val log: Logger = LogManager.getLogger(Renderer::class.java.name)
    }

    private lateinit var font: Font
    private lateinit var quadMesh: Mesh
    private lateinit var cubeMesh: Mesh

    fun init() {
        glEnable(GL_DEPTH_TEST)

        create1x1WhiteTexture()

        font = Font.fromInternalFile("fonts/RobotoMono/RobotoMono-Regular.ttf")
        quadMesh = MeshUtils.quad()
        cubeMesh = MeshUtils.unitCube()
    }

    private fun create1x1WhiteTexture() {
        val textureId = glGenTextures()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        var buf: ByteBuffer? = null
        try {
            val byte = (0xFF).toByte()
            buf = MemoryUtil.memAlloc(4)
                    .put(byte)
                    .put(byte)
                    .put(byte)
                    .put(byte)
                    .flip()
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)
        } finally {
            MemoryUtil.memFree(buf)
        }
    }

    fun clear() {
        glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun mesh(shader: Shader, mesh: Mesh, transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        shader.bind()
        shader.setUniform("model", transformation)
        shader.setUniform("color", color)

        if (mesh.hasTexture()) {
            shader.setUniform("textureSampler", 1)
        } else {
            shader.setUniform("textureSampler", 0)
        }

        mesh.bind()

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        shader.unbind()
    }

    fun quad(
            shader: Shader,
            transformation: Matrix4f = Matrix4f(),
            color: Vector4f = Vector4f(1.0F)
    ) {
        this.mesh(shader, quadMesh, transformation, color)
    }

    fun cube(shader: Shader, transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        this.mesh(shader, cubeMesh, transformation, color)
    }

    fun line(shader: Shader, start: Vector3f, end: Vector3f, color: Vector4f) {
        shader.bind()
        shader.setUniform("model", Matrix4f())
        shader.setUniform("textureSampler", 0)
        shader.setUniform("color", color)

        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val vertices = listOf(
                start.x, start.y, start.z,
                end.x, end.y, end.z
        )

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(vertices.size)
            buffer.put(vertices.toFloatArray()).flip()

            val vbo = GL20.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo)
            GL20.glBufferData(GL20.GL_ARRAY_BUFFER, buffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            if (buffer != null) {
                MemoryUtil.memFree(buffer)
            }
        }

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_LINES, 0, 2)

        shader.unbind()
    }

    fun wireframe(activate: Boolean = true, func: () -> Unit) {
        if (activate) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        }
        func()
        if (activate) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        }
    }

    fun text(shader: Shader, text: String, transformation: Matrix4f, color: Vector4f = Vector4f(1.0F)) {
        val mesh = MeshUtils.text(text, font)
        this.mesh(shader, mesh, transformation, color)
    }
}
