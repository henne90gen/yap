package de.yap.engine.graphics

import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector3i
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.stb.STBEasyFont
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

class Text(var value: String, val font: Font, val transform: Matrix4f, val color: Vector4f = Vector4f(1.0F)) {

    var mesh: Mesh

    init {
        mesh = MeshUtils.text(font, value)
    }

    fun updateString(value: String) {
        if (this.value == value) {
            return
        }

        this.value = value
        mesh = MeshUtils.text(font, value)
    }
}

class FontRenderer {

    lateinit var font: Font
    lateinit var hudShader: Shader
    var aspectRatio: Float = 1.0F

    fun init() {
        font = Font.fromInternalFile("fonts/RobotoMono/RobotoMono-Regular.ttf")
        hudShader = Shader("HUD", "shaders/hud_vertex.glsl", "shaders/hud_fragment.glsl")
        hudShader.compile()
    }

    fun stringSimple(string: String, transform: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        hudShader.bind()
        hudShader.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))
        hudShader.setUniform("color", color)
        hudShader.setUniform("model", Matrix4f(transform).scale(0.01F, -0.01F, 1.0F))
        hudShader.setUniform("textureSampler", 0)

        val averageBytesPerCharacter = 270
        val vertexBuffer = BufferUtils.createByteBuffer(string.length * averageBytesPerCharacter)
        val quadCount = STBEasyFont.stb_easy_font_print(0.0F, 0.0F, string, null, vertexBuffer)

        val indices = mutableListOf<Vector3i>()
        for (quadIndex in 0..quadCount) {
            val startIndex = quadIndex * 4
            indices.add(Vector3i(startIndex, startIndex + 1, startIndex + 2))
            indices.add(Vector3i(startIndex, startIndex + 2, startIndex + 3))
        }

        val vao = glGenVertexArrays()
        glBindVertexArray(vao)

        val vbo = GL20.glGenBuffers()
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vbo)
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexBuffer, GL20.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        val stride = 3 * 4 + 4
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, stride, 0)

        GL20.glEnableVertexAttribArray(2)
        val offset: Long = 3 * 4
        GL20.glVertexAttribPointer(2, 4, GL20.GL_UNSIGNED_BYTE, false, stride, offset)

        var buffer: IntBuffer? = null
        try {
            buffer = MemoryUtil.memAllocInt(indices.size * 3)
            for (d in indices) {
                buffer.put(d.x)
                buffer.put(d.y)
                buffer.put(d.z)
            }
            buffer.flip()

            val ibo = GL15.glGenBuffers()
            GL20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, ibo)
            GL20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer!!, GL20.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(buffer)
        }

        GL13.glDrawElements(GL13.GL_TRIANGLES, indices.size * 3, GL13.GL_UNSIGNED_INT, 0)

        hudShader.unbind()
    }

    fun quad(transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        this.mesh(YapGame.getInstance().renderer.quadMesh, transformation, color)
    }

    fun mesh(mesh: Mesh, transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        hudShader.bind()
        hudShader.setUniform("model", transformation)
        hudShader.setUniform("color", color)

        if (mesh.hasTexture()) {
            hudShader.setUniform("textureSampler", 1)
        } else {
            hudShader.setUniform("textureSampler", 0)
        }

        mesh.bind()

        GL13.glDrawElements(GL13.GL_TRIANGLES, mesh.indices.size * 3, GL13.GL_UNSIGNED_INT, 0)

        hudShader.unbind()
    }
}
