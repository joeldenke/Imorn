package se.imorn.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalImornColors = staticCompositionLocalOf { ImornLightColors.toImornColors() }
private val LocalImornTypography = staticCompositionLocalOf { ImornTextStyles }
private val LocalImornShapes = staticCompositionLocalOf { ImornCorners }
private val LocalImornSpacing = staticCompositionLocalOf { ImornSpacing }

/**
 * Root theme for the app. Provides Imorn's own tokens, and wraps [MaterialTheme] with the same values so the
 * Material 3 components inside :core:designsystem pick them up. Material 3 is an implementation detail of this
 * module: nothing in the public API is a Material type, so it can be swapped component by component.
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
    val colors = remember(colorScheme) { colorScheme.toImornColors() }
    CompositionLocalProvider(
        LocalImornColors provides colors,
        LocalImornTypography provides ImornTextStyles,
        LocalImornShapes provides ImornCorners,
        LocalImornSpacing provides ImornSpacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTypography,
            shapes = MaterialShapes,
            content = content,
        )
    }
}

private val MaterialTypography = ImornTextStyles.toMaterial()
private val MaterialShapes = ImornCorners.toMaterial()

/** Single entry point for design tokens in feature code. */
object ImornTheme {
    val colors: ImornColors
        @Composable @ReadOnlyComposable
        get() = LocalImornColors.current

    val typography: ImornTypography
        @Composable @ReadOnlyComposable
        get() = LocalImornTypography.current

    val shapes: ImornShapes
        @Composable @ReadOnlyComposable
        get() = LocalImornShapes.current

    val spacing: ImornSpacing
        @Composable @ReadOnlyComposable
        get() = LocalImornSpacing.current
}
