package se.imorn.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalImornSpacing = staticCompositionLocalOf { ImornSpacing }

/**
 * Root theme for the app. Wraps [MaterialTheme] so Material 3 components pick up Imorn tokens, and adds the
 * spacing tokens Material doesn't have.
 *
 * @param dynamicColor use the wallpaper colors on Android 12+ instead of the brand palette. Off by default so
 *   screens look the same everywhere unless the user asks for it.
 */
@Composable
fun ImornTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> ImornDarkColors

        else -> ImornLightColors
    }
    CompositionLocalProvider(LocalImornSpacing provides ImornSpacing) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ImornTypography,
            shapes = ImornShapes,
            content = content,
        )
    }
}

/** Single entry point for design tokens in feature code. */
object ImornTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: ImornSpacing
        @Composable @ReadOnlyComposable
        get() = LocalImornSpacing.current
}
