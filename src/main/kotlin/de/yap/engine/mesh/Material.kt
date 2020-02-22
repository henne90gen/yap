package de.yap.engine.mesh

import de.yap.engine.Texture

class Material(val name: String) {

    var texture: Texture? = null

    fun bind() {
        if (texture == null) {
            return
        }

        texture?.bind()
    }

    fun hasTexture(): Boolean {
        return texture != null
    }
}
