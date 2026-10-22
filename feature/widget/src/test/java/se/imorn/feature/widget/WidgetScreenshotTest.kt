package se.imorn.feature.widget

import android.os.Build
import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
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
    val roborazziRule = com.github.takahirom.roborazzi.RoborazziRule(
        options = com.github.takahirom.roborazzi.RoborazziRule.Options(
            captureRoot = com.github.takahirom.roborazzi.captureRoboImage,
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
