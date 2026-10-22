package se.imorn.data

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.AppSettings
import se.imorn.model.ThemeMode

@RunWith(RobolectricTestRunner::class)
class SettingsStoreTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val stores = StoreScope()

    @After fun close() = stores.close()

    @Test fun `defaults until something is chosen`() {
        assertEquals(AppSettings(), SettingsStore(context, stores.scope, stores.cipher).settings.await())
    }

    @Test fun `chosen settings survive a restart`() {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setTheme(ThemeMode.Dark)
        store.setDynamicColor(true)
        val chosen = AppSettings(theme = ThemeMode.Dark, dynamicColor = true)
        store.settings.await { it == chosen }

        stores.restart()
        assertEquals(chosen, SettingsStore(context, stores.scope, stores.cipher).settings.await())
    }

    @Test fun `the welcome stays done after a restart`() {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setOnboarded(true)
        store.settings.await { it.onboarded }

        stores.restart()
        assertEquals(true, SettingsStore(context, stores.scope, stores.cipher).settings.await().onboarded)
    }

    @Test fun `writes apply in order`() {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setTheme(ThemeMode.Dark)
        store.setTheme(ThemeMode.Light)
        store.setTheme(ThemeMode.Dark)
        // The last write in the queue: once it shows, every write before it has been applied, in order.
        store.setDynamicColor(true)
        store.settings.await { it.dynamicColor }

        stores.restart()
        assertEquals(ThemeMode.Dark, SettingsStore(context, stores.scope, stores.cipher).settings.await().theme)
    }
}
