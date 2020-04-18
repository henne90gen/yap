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
                .translate(0.2F, 0.5F, 0.0F)
        fontRenderer.stringSimple("Hello World", transform)

        val redTransform = Matrix4f()
                .translate(0.2F, 0.4F, 0.0F)
        fontRenderer.stringSimple("Red", redTransform, Vector4f(0.0F, 1.0F, 1.0F, 1.0F))

        val greenTransform = Matrix4f()
                .translate(0.2F, 0.3F, 0.0F)
        fontRenderer.stringSimple("Green", greenTransform, Vector4f(1.0F, 0.0F, 1.0F, 1.0F))

        YapGame.getInstance().renderer.wireframe {
            val blueTransform = Matrix4f()
                    .translate(0.2F, 0.2F, 0.0F)
            fontRenderer.stringSimple("Blue", blueTransform, Vector4f(1.0F, 1.0F, 0.0F, 1.0F))
        }

        val mesh = MeshUtils.quad2D(material = fontRenderer.font.material)
        val hudShader = fontRenderer.hudShader
        hudShader.bind()
        val view = Matrix4f()
                .scale(1.0F / fontRenderer.aspectRatio, 1.0F, 1.0F)
        hudShader.setUniform("view", view)
        hudShader.setUniform("model", Matrix4f().translate(-1.0F, 0.0F, 0.0F))
        hudShader.setUniform("color", Vector4f(1.0F))
        hudShader.setUniform("textureSampler", 1)

        mesh.bind()

        glDrawElements(GL_TRIANGLES, mesh.indices.size * 3, GL_UNSIGNED_INT, 0)

        hudShader.unbind()
    }
}
