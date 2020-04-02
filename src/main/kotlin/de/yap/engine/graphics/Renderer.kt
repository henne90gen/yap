package de.yap.engine.graphics

import de.yap.engine.mesh.Material
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
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
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer


class Renderer {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private lateinit var quadMesh: Mesh
    private lateinit var cubeMesh: Mesh
    var textureMapMaterial: Material = Material("EmptyMaterial")

    val shader3D = Shader("shaders/vertex.glsl", "shaders/fragment.glsl")

    fun init() {
        glEnable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        shader3D.compile()

        create1x1WhiteTexture()

        quadMesh = MeshUtils.quad2D()
        cubeMesh = MeshUtils.unitCube()

        textureMapMaterial = Material("CubeMaterial")
        textureMapMaterial.texture = Texture.fromFile(File("models/texture_atlas.png"))
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

    fun mesh(mesh: Mesh, transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        shader3D.bind()
        shader3D.setUniform("model", transformation)
        shader3D.setUniform("color", color)

        if (mesh.hasTexture()) {
            shader3D.setUniform("textureSampler", 1)
        } else {
            shader3D.setUniform("textureSampler", 0)
        }

        mesh.bind()

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        shader3D.unbind()
    }

    fun quad(transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        this.mesh(quadMesh, transformation, color)
    }

    fun cube(transformation: Matrix4f = Matrix4f(), color: Vector4f = Vector4f(1.0F)) {
        this.mesh(cubeMesh, transformation, color)
    }

    fun line(start: Vector3f, end: Vector3f, color: Vector4f) {
        shader3D.bind()
        shader3D.setUniform("model", Matrix4f())
        shader3D.setUniform("textureSampler", 0)
        shader3D.setUniform("color", color)

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
            MemoryUtil.memFree(buffer)
        }

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0)

        glDrawArrays(GL_LINES, 0, 2)

        shader3D.unbind()
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

    fun disableDepthTest(function: () -> Unit) {
        glDisable(GL_DEPTH_TEST)
        function()
        glEnable(GL_DEPTH_TEST)
    }

    fun inScreenSpace(func: () -> Unit) {
        val aspectRatio = YapGame.getInstance().window.aspectRatio()
        val projection = shader3D.getUniform<Matrix4fUniform>("projection")!!.value
        val view = shader3D.getUniform<Matrix4fUniform>("view")!!.value
        shader3D.setUniform("projection", Matrix4f())
        shader3D.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))

        func()

        shader3D.setUniform("projection", projection)
        shader3D.setUniform("view", view)
    }
}
