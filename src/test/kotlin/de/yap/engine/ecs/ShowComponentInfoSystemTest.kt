package de.yap.engine.ecs

import de.yap.engine.ecs.systems.ShowComponentInfoSystem
import org.joml.Vector3f
import org.junit.Test
import kotlin.test.assertEquals

class ShowComponentInfoTestComponent(
        var position: Vector3f = Vector3f(1.0F)
) : Component()

class ShowComponentInfoSystemTest {
    @Test
    fun testComponentToText() {
        val result = ShowComponentInfoSystem.componentToText(ShowComponentInfoTestComponent())
        val expected = """ShowComponentInfoTestComponent
            |    position: ( 1.000E+0  1.000E+0  1.000E+0)
            |""".trimMargin()
        assertEquals(expected, result)
    }
}
