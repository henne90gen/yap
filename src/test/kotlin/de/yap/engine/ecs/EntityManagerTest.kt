package de.yap.engine.ecs

import org.junit.Test
import kotlin.test.assertEquals

class TestComponent : Component()

class TestSystem : System(TestComponent::class.java) {
    var initCounter = 0
    var updateCounter = 0
    var updateEntityCount = 0
    var renderCounter = 0
    var renderEntityCount = 0

    override fun init() {
        initCounter++
    }

    override fun update(entities: List<Entity>) {
        updateCounter++
        updateEntityCount += entities.size
    }

    override fun render(entities: List<Entity>) {
        renderCounter++
        renderEntityCount += entities.size
    }
}

class EntityManagerTest {

    @Test
    fun testEntityManager() {
        val manager = EntityManager()
        val testSystem = TestSystem()
        manager.register(testSystem)

        manager.init()
        manager.update()
        manager.render()

        assertEquals(1, testSystem.initCounter)
        assertEquals(1, testSystem.updateCounter)
        assertEquals(0, testSystem.updateEntityCount)
        assertEquals(1, testSystem.renderCounter)
        assertEquals(0, testSystem.renderEntityCount)
    }
}
