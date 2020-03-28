package de.yap.engine.debug

class DebugFontTexture {

    // TODO use EntityManager
//    @Subscribe
//    fun renderFontTexture(event: RenderEvent) {
//        val yapGame = YapGame.getInstance()
//        val fontRenderer = yapGame.fontRenderer
//        val mesh = MeshUtils.quad2D(material = fontRenderer.font.material)
//        val fontShader = fontRenderer.fontShader
//        fontShader.bind()
//        fontShader.setUniform("view", Matrix4f().scale(1.0F / fontRenderer.aspectRatio, 1.0F, 1.0F))
//        fontShader.setUniform("projection", Matrix4f())
//        fontShader.setUniform("model", Matrix4f())
//        fontShader.setUniform("color", Vector4f(1.0F))
//        fontShader.setUniform("textureSampler", 1)
//
//        mesh.bind()
//
//        GL13.glDrawElements(GL13.GL_TRIANGLES, mesh.indices.size * 3, GL13.GL_UNSIGNED_INT, 0)
//
//        fontShader.unbind()
//    }
}
