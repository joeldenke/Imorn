package se.imorn.content

import org.junit.Assert.assertEquals
import org.junit.Test
import se.imorn.model.LifeStage

class SuggestedFirstTest {
    private fun template(id: String, vararg stages: String) = Template(
        id = id,
        version = 1,
        title = mapOf(SWEDISH to id, ENGLISH to id),
        description = mapOf(SWEDISH to id, ENGLISH to id),
        helps = mapOf(SWEDISH to id, ENGLISH to id),
        category = TemplateCategory.LifeEvents,
        lifestages = stages.toList(),
    )

    private val templates = listOf(
        template("pension", "next-chapter"),
        template("move", "building", "first-home"),
        template("baby", "building"),
        template("estate"),
    )

    @Test fun `templates for the user's stages come first, in the pack's order`() {
        val ordered = templates.suggestedFirst(setOf(LifeStage.BuildingUp))
        assertEquals(listOf("move", "baby", "pension", "estate"), ordered.map { it.id })
    }

    @Test fun `without stages the order is the pack's`() {
        assertEquals(templates, templates.suggestedFirst(emptySet()))
    }
}
