package se.imorn.feature.widget

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImornWidgetTest {
    @Test
    fun widgetColorsUseGryningPalette() {
        assert(WidgetColors.accent != null)
        assert(WidgetColors.accentAlt != null)
        assert(WidgetColors.background != null)
        assert(WidgetColors.textPrimary != null)
    }
}
