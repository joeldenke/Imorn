package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LifeStageTest {
    @Test fun `every stage reads back from its id, and an unknown id is null`() {
        LifeStage.entries.forEach { assertEquals(it, LifeStage.fromId(it.id)) }
        assertNull(LifeStage.fromId("retired-pirate"))
    }

    @Test fun `ids are unique`() {
        assertEquals(LifeStage.entries.size, LifeStage.entries.map { it.id }.toSet().size)
    }
}
