package se.imorn.data

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.LifeStage
import se.imorn.model.Profile

@RunWith(RobolectricTestRunner::class)
class ProfileStoreTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val stores = StoreScope()

    @After fun close() = stores.close()

    @Test fun `empty until something is saved`() {
        assertEquals(Profile(), ProfileStore(context, stores.scope, stores.cipher).profile.await())
    }

    @Test fun `a saved profile survives a restart`() {
        val store = ProfileStore(context, stores.scope, stores.cipher)
        val profile = Profile(name = "Alex", lifeStages = setOf(LifeStage.BuildingUp, LifeStage.NewInSweden))
        store.save(profile)
        store.profile.await { it == profile }

        stores.restart()
        assertEquals(profile, ProfileStore(context, stores.scope, stores.cipher).profile.await())
    }

    @Test fun `a blank name is no name, and names are trimmed`() {
        val store = ProfileStore(context, stores.scope, stores.cipher)
        store.save(Profile(name = "  Alex "))
        assertEquals("Alex", store.profile.await { it.name != null }.name)
        store.save(Profile(name = "   "))
        assertEquals(null, store.profile.await { it.name == null }.name)
    }
}
