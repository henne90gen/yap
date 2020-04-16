package de.yap.engine.debug

import de.yap.engine.ecs.entities.Entity
import de.yap.engine.ecs.systems.ISystem
import de.yap.engine.mesh.MeshUtils
import de.yap.game.YapGame
import org.joml.Matrix4f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11.*

class DebugFont : ISystem() {

    var enabled = false

    override fun render(entities: List<Entity>) {
        if (!enabled) {
            return
        }

        val yapGame = YapGame.getInstance()
        val fontRenderer = yapGame.fontRenderer

        val transform = Matrix4f()
                .translate(-0.5F, 0.0F, 0.0F)
        fontRenderer.stringSimple("Hello World", transform)

        val mesh = MeshUtils.quad2D(material = fontRenderer.font.material)
        val fontShader = fontRenderer.hudShader
        fontShader.bind()
        val view = Matrix4f()
                .scale(1.0F / fontRenderer.aspectRatio, 1.0F, 1.0F)
        fontShader.setUniform("view", view)
        fontShader.setUniform("model", Matrix4f().translate(-1.0F, 0.5F, 0.0F))
        fontShader.setUniform("color", Vector4f(1.0F))
        fontShader.setUniform("textureSampler", 1)

        mesh.bind()

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        fontShader.unbind()
    }
}
