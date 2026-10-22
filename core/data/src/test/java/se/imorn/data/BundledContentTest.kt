package se.imorn.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.content.ContentValidator

/** The pack as the app reads it from its assets: the same files as content/, and valid. */
@RunWith(RobolectricTestRunner::class)
class BundledContentTest {
    @Test fun `the app ships the pack from content`() = runBlocking {
        val pack = BundledContent(RuntimeEnvironment.getApplication()).pack()
        assertTrue(pack.template("move") != null)
        assertTrue(pack.link("skatteverket.flytt") != null)
        assertEquals(emptyList<String>(), ContentValidator.validate(pack))
    }
}
