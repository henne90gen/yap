package de.yap.engine.graphics

import org.joml.Vector2f

val WHITE = TextureCoords.fromIndex(0, 0)
val BLACK = TextureCoords.fromIndex(0, 7)
val DARK_GREY = TextureCoords.fromIndex(0, 6)
val LIGHT_GREY = TextureCoords.fromIndex(0, 2)
val GREY = TextureCoords.fromIndex(0, 4)

val RED = TextureCoords.fromIndex(1, 2)
val BLUE = TextureCoords.fromIndex(2, 1)
val GREEN = TextureCoords.fromIndex(6, 1)
val YELLOW = TextureCoords.fromIndex(6, 3)

val CHECKER_BOARD = TextureCoords.fromIndex(8, 0)
val PLANKS = TextureCoords.fromIndex(9, 0)

data class TextureCoords(val texMin: Vector2f, val texMax: Vector2f) {
    companion object {
        fun fromIndex(x: Int, y: Int): TextureCoords {
            // assumes using 32x32px Textures
            val texMin = Vector2f(1F/32F * x, 1F/32F * y)
            val texMax = Vector2f(1F/32F * (x+1), 1F/32F * (y+1))
            return TextureCoords(texMin, texMax)
        }
    }
}
