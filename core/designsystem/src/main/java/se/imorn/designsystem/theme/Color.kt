package se.imorn.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Imorn's palette. A calm, local-first tool: a deep pine primary, a warm sand accent.
// Every value here is referenced through ImornTheme.colors, never hardcoded in features.

val Pine40 = Color(0xFF2F5D50)
val Pine80 = Color(0xFF9CD3BE)
val Sand40 = Color(0xFF7A5B2E)
val Sand80 = Color(0xFFE8C48A)
val Clay40 = Color(0xFF7C4E43)
val Clay80 = Color(0xFFF0B7A8)

/**
 * The color roles feature code may use, read through ImornTheme.colors. Imorn's own type, so features never see
 * Material 3: the theme maps a full Material [ColorScheme] to these, and only :core:designsystem knows Material.
 */
@Immutable
data class ImornColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainer: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
)

/** Picks Imorn's roles from a scheme, so they also follow dynamic color when it's on. */
internal fun ColorScheme.toImornColors() = ImornColors(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceContainer = surfaceContainer,
    outline = outline,
    error = error,
    onError = onError,
)

// Every role is set, also the ones Imorn doesn't read itself: Material components use them (dialogs, sheets,
// navigation bars, text fields), and an unset role falls back to Material's purple baseline.
internal val ImornLightColors = lightColorScheme(
    primary = Pine40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4EED6),
    onPrimaryContainer = Color(0xFF002016),
    inversePrimary = Pine80,
    secondary = Sand40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB0),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Clay40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF31120C),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFAFDF9),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404943),
    surfaceTint = Pine40,
    surfaceBright = Color(0xFFFAFDF9),
    surfaceDim = Color(0xFFD9DBD8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F7F3),
    surfaceContainer = Color(0xFFEEF2ED),
    surfaceContainerHigh = Color(0xFFE9ECE8),
    surfaceContainerHighest = Color(0xFFE3E6E2),
    inverseSurface = Color(0xFF2E312F),
    inverseOnSurface = Color(0xFFEFF1ED),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2),
)

internal val ImornDarkColors = darkColorScheme(
    primary = Pine80,
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF14513F),
    onPrimaryContainer = Color(0xFFB4EED6),
    inversePrimary = Pine40,
    secondary = Sand80,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF614200),
    onSecondaryContainer = Color(0xFFFFDDB0),
    tertiary = Clay80,
    onTertiary = Color(0xFF4A2820),
    tertiaryContainer = Color(0xFF653E34),
    onTertiaryContainer = Color(0xFFFFDAD2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFC9C2),
    surfaceTint = Pine80,
    surfaceBright = Color(0xFF373A38),
    surfaceDim = Color(0xFF111412),
    surfaceContainerLowest = Color(0xFF0C0F0D),
    surfaceContainerLow = Color(0xFF1D201E),
    surfaceContainer = Color(0xFF212422),
    surfaceContainerHigh = Color(0xFF2B2E2C),
    surfaceContainerHighest = Color(0xFF363937),
    inverseSurface = Color(0xFFE1E3DF),
    inverseOnSurface = Color(0xFF2E312F),
    outline = Color(0xFF89938C),
    outlineVariant = Color(0xFF404943),
)
