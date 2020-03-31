package de.yap.engine.graphics

import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.joml.Matrix4f
import org.joml.Vector4f
import org.lwjgl.opengl.GL13

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
    lateinit var fontShader: Shader
    var aspectRatio: Float = 1.0F

    fun init() {
        font = Font.fromInternalFile("fonts/RobotoMono/RobotoMono-Regular.ttf")
        fontShader = Shader("shaders/vertex.glsl", "shaders/font_fragment.glsl")
        fontShader.compile()
    }

    fun string(string: String, transform: Matrix4f, color: Vector4f = Vector4f(1.0F)) {
        fontShader.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))
        fontShader.setUniform("projection", Matrix4f())
        val mesh = MeshUtils.text(font, string)
        renderString(mesh, transform, color)
    }

    fun string(text: Text) {
        fontShader.setUniform("view", Matrix4f().scale(1.0F / aspectRatio, 1.0F, 1.0F))
        fontShader.setUniform("projection", Matrix4f())
        renderString(text.mesh, text.transform, text.color)
    }

    fun stringInScene(text: Text, view: Matrix4f, projection: Matrix4f) {
        fontShader.setUniform("view", view)
        fontShader.setUniform("projection", projection)
        renderString(text.mesh, text.transform, text.color)
    }

    private fun renderString(mesh: Mesh, transform: Matrix4f, color: Vector4f = Vector4f(1.0F)) {
        fontShader.bind()
        fontShader.setUniform("color", color)
        fontShader.setUniform("model", transform)
        fontShader.setUniform("textureSampler", 1)

        mesh.bind()

        GL13.glDrawElements(GL13.GL_TRIANGLES, mesh.indices.size * 3, GL13.GL_UNSIGNED_INT, 0)

        fontShader.unbind()
    }
}
