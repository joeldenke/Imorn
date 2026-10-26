package se.imorn.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Imorn's palette, "Gryning", on warm paper: the bookshelf look. A deep indigo for the future and the overview, a
// soft apricot for what happens now, and a rose for memories, on a cream ground with ink-dark text.
// Every value here is referenced through ImornTheme.colors, never hardcoded in features.

val Indigo40 = Color(0xFF2E3A8C)
val Indigo80 = Color(0xFFB9C1F2)
val Apricot40 = Color(0xFF9A4F12)
val Apricot80 = Color(0xFFFFB98A)
val Rose40 = Color(0xFFA04A6C)
val Rose80 = Color(0xFFF2A9C3)

/** Ink: the text and the strongest buttons on paper. */
val Ink = Color(0xFF22201C)

/** Paper: the ground, and the text on ink. */
val Paper = Color(0xFFFAF6F0)

// Brand colors outside the Material scheme. Night is the launcher icon's plate (res/values/colors.xml,
// ic_launcher_background, must match); Dawn is the rising sun, for brand moments such as onboarding.
val Night = Color(0xFF1D2A5E)
val Dawn = Color(0xFFFFB067)

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
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
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

/**
 * Cards sit on the background as a lighter surface with a hairline outline, as in Novila: white on the light
 * background, a step lighter than the night in dark mode. Fixed for both palettes, also with dynamic color.
 */
@Immutable
data class ImornSurfaceColors(
    val card: Color,
    val cardOutline: Color,
)

internal val ImornLightSurfaces = ImornSurfaceColors(card = Color.White, cardOutline = Color(0xFFEDE4D8))
internal val ImornDarkSurfaces = ImornSurfaceColors(card = Color(0xFF211F27), cardOutline = Color(0xFF34313B))

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
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
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
    primary = Indigo40,
    onPrimary = Paper,
    primaryContainer = Color(0xFFDDE0F4),
    onPrimaryContainer = Color(0xFF141B52),
    inversePrimary = Indigo80,
    secondary = Apricot40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8B5),
    onSecondaryContainer = Ink,
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6D3E0),
    onTertiaryContainer = Color(0xFF3A1626),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7DCCD),
    onSurfaceVariant = Color(0xFF645D56),
    surfaceTint = Indigo40,
    surfaceBright = Paper,
    surfaceDim = Color(0xFFE2D8CA),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5EFE7),
    surfaceContainer = Color(0xFFEFE7DC),
    surfaceContainerHigh = Color(0xFFE9E0D3),
    surfaceContainerHighest = Color(0xFFE2D8CA),
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    outline = Color(0xFF8E857A),
    outlineVariant = Color(0xFFDDD3C6),
)

internal val ImornDarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF1A2266),
    primaryContainer = Indigo40,
    onPrimaryContainer = Color(0xFFDDE0F4),
    inversePrimary = Indigo40,
    secondary = Apricot80,
    onSecondary = Color(0xFF4A2400),
    secondaryContainer = Color(0xFF6B3A12),
    onSecondaryContainer = Color(0xFFFFE3CC),
    tertiary = Rose80,
    onTertiary = Color(0xFF4F1631),
    tertiaryContainer = Color(0xFF6E2E4A),
    onTertiaryContainer = Color(0xFFFFD9E6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF15141B),
    onBackground = Color(0xFFF1ECE4),
    surface = Color(0xFF15141B),
    onSurface = Color(0xFFF1ECE4),
    surfaceVariant = Color(0xFF3A3640),
    onSurfaceVariant = Color(0xFFCFC7BD),
    surfaceTint = Indigo80,
    surfaceBright = Color(0xFF3B3842),
    surfaceDim = Color(0xFF15141B),
    surfaceContainerLowest = Color(0xFF100F14),
    surfaceContainerLow = Color(0xFF1C1B22),
    surfaceContainer = Color(0xFF242229),
    surfaceContainerHigh = Color(0xFF2D2B33),
    surfaceContainerHighest = Color(0xFF37343D),
    inverseSurface = Color(0xFFF1ECE4),
    inverseOnSurface = Color(0xFF22201C),
    outline = Color(0xFF8E877E),
    outlineVariant = Color(0xFF45414B),
)
