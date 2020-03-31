package de.yap.engine.mesh

import de.yap.engine.graphics.Texture

class Material(val name: String) {

    var texture: Texture? = null

    fun bind() {
        texture?.bind()
    }

    fun hasTexture(): Boolean {
        return texture != null
    }
}
