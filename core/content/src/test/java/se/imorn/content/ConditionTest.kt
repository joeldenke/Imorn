package se.imorn.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionTest {
    private val rental = mapOf("housing" to Answer.Choice("rental"))

    @Test fun `no condition always applies`() {
        assertTrue((null as Condition?).appliesTo(emptyMap()))
    }

    @Test fun `equals and in compare the chosen option`() {
        assertTrue(Condition("housing", equals = "rental").appliesTo(rental))
        assertFalse(Condition("housing", equals = "house").appliesTo(rental))
        assertTrue(Condition("housing", isIn = listOf("rental", "condo")).appliesTo(rental))
        assertFalse(Condition("housing", isIn = listOf("house")).appliesTo(rental))
    }

    @Test fun `isTrue compares a yes or no`() {
        assertTrue(Condition("kids", isTrue = true).appliesTo(mapOf("kids" to Answer.YesNo(true))))
        assertFalse(Condition("kids", isTrue = true).appliesTo(mapOf("kids" to Answer.YesNo(false))))
    }

    @Test fun `an unanswered question keeps the step`() {
        assertTrue(Condition("housing", equals = "house").appliesTo(emptyMap()))
    }
}
