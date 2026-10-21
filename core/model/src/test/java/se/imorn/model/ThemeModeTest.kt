package se.imorn.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test fun `system follows the phone`() {
        assertTrue(ThemeMode.System.isDark(systemDark = true))
        assertFalse(ThemeMode.System.isDark(systemDark = false))
    }

    @Test fun `light and dark ignore the phone`() {
        assertFalse(ThemeMode.Light.isDark(systemDark = true))
        assertTrue(ThemeMode.Dark.isDark(systemDark = false))
    }

    @Test fun `a saved name reads back, and a missing or unknown one is System`() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromName(it.name)) }
        assertEquals(ThemeMode.System, ThemeMode.fromName(null))
        assertEquals(ThemeMode.System, ThemeMode.fromName("Sepia"))
    }
}
