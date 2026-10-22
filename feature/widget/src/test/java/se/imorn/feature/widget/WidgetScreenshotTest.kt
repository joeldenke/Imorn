package se.imorn.feature.widget

import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import se.imorn.designsystem.theme.ImornTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel6)
class WidgetScreenshotTest {
    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            captureRoot = { it.captureRoboImage() },
        ),
    )

    @Test
    fun widgetWithNextStep() {
        captureRoboImage {
            ImornTheme {
                WidgetContent(
                    chapterId = "preview-move",
                    stepId = "address-change",
                    title = "Report your move",
                    hasStep = true,
                )
            }
        }
    }

    @Test
    fun widgetEmpty() {
        captureRoboImage {
            ImornTheme {
                WidgetContent(
                    chapterId = null,
                    stepId = null,
                    title = null,
                    hasStep = false,
                )
            }
        }
    }
}
