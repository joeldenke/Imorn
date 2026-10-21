package se.imorn.data

import android.content.Context
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.imorn.model.AppSettings
import se.imorn.model.ThemeMode
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * How the stores' files look and behave on disk: sealed with the cipher, and replaced with empty data when they
 * cannot be opened (another key, a truncated write, a file without Imorn's header).
 */
@RunWith(RobolectricTestRunner::class)
class EncryptedDataStoreTest {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val stores = StoreScope()
    private val file = context.preferencesDataStoreFile(SETTINGS)

    @After fun close() = stores.close()

    @Test fun `a saved file does not carry the values in the clear`() = runBlocking {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setTheme(ThemeMode.Dark)
        store.settings.await { it.theme == ThemeMode.Dark }

        val bytes = file.awaitWritten()
        assertEquals("the file must start with Imorn's header", HEADER, bytes.first())
        assertFalse("the theme must not be readable in the file", bytes.contains("Dark".toByteArray()))
    }

    @Test fun `a file sealed with another key reads as empty instead of failing`() = runBlocking {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setTheme(ThemeMode.Dark)
        store.settings.await { it.theme == ThemeMode.Dark }
        stores.restart()

        // A cipher whose key does not match the one the file was sealed with (as after a restore onto a new phone).
        val other = SettingsStore(context, stores.scope, testCipher())
        assertEquals(AppSettings(), other.settings.await())
    }

    @Test fun `a truncated file reads as empty instead of failing`() = runBlocking {
        val store = SettingsStore(context, stores.scope, stores.cipher)
        store.setTheme(ThemeMode.Dark)
        store.settings.await { it.theme == ThemeMode.Dark }
        val bytes = file.awaitWritten()
        file.writeBytes(bytes.copyOfRange(0, bytes.size / 2))
        stores.restart()

        assertEquals(AppSettings(), SettingsStore(context, stores.scope, stores.cipher).settings.await())
    }

    @Test fun `a plaintext file reads as empty, since Imorn never wrote one`() = runBlocking {
        file.parentFile?.mkdirs()
        file.outputStream().use {
            PreferencesFileSerializer.writeTo(preferencesOf(stringPreferencesKey("theme") to ThemeMode.Dark.name), it)
        }

        assertEquals(AppSettings(), SettingsStore(context, stores.scope, stores.cipher).settings.await())
    }

    private suspend fun File.awaitWritten(): ByteArray {
        val deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)
        while (System.currentTimeMillis() < deadline) {
            takeIf { it.isFile && it.length() > 0 }?.readBytes()?.let { return it }
            delay(25)
        }
        error("$this was never written")
    }

    private fun ByteArray.contains(expected: ByteArray): Boolean =
        expected.isNotEmpty() && (0..size - expected.size).any { start ->
            expected.indices.all { this[start + it] == expected[it] }
        }

    private companion object {
        /** `SettingsStore`'s DataStore file name (its companion is private). */
        const val SETTINGS = "settings"

        /** The first byte of Imorn's header, `I`. */
        const val HEADER = 0x49.toByte()
    }
}
