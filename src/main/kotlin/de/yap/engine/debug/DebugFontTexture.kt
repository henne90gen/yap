package de.yap.engine.debug

import de.yap.engine.ecs.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11.*

class DebugFontTexture : ISystem() {

    private var enabled = false

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        val yapGame = YapGame.getInstance()
        val fontRenderer = yapGame.fontRenderer
        val mesh = MeshUtils.quad2D(material = fontRenderer.font.material)
        val fontShader = fontRenderer.fontShader
        fontShader.bind()
        fontShader.setUniform("view", Matrix4f().scale(1.0F / fontRenderer.aspectRatio, 1.0F, 1.0F))
        fontShader.setUniform("projection", Matrix4f())
        fontShader.setUniform("model", Matrix4f())
        fontShader.setUniform("color", Vector4f(1.0F))
        fontShader.setUniform("textureSampler", 1)

        mesh.bind()

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        fontShader.unbind()
    }
}
