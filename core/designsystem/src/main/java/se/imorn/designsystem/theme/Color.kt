package se.imorn.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Imorn's palette, "Gryning": the sky just before the sun comes up. A calm night indigo for the future and the
// overview, a warm apricot for what happens along the way, and a rose for memories.
// Every value here is referenced through ImornTheme.colors, never hardcoded in features.

val Indigo40 = Color(0xFF3A4BA8)
val Indigo80 = Color(0xFFBAC3FF)
val Apricot40 = Color(0xFF8B5000)
val Apricot80 = Color(0xFFFFB77A)
val Rose40 = Color(0xFF8E4A63)
val Rose80 = Color(0xFFFFB0C9)

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

internal val ImornLightSurfaces = ImornSurfaceColors(card = Color.White, cardOutline = Color(0xFFE4E1E9))
internal val ImornDarkSurfaces = ImornSurfaceColors(card = Color(0xFF1F1F25), cardOutline = Color(0xFF303036))

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
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF00115A),
    inversePrimary = Indigo80,
    secondary = Apricot40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E3),
    onTertiaryContainer = Color(0xFF3A0720),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF8FD),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FD),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    surfaceTint = Indigo40,
    surfaceBright = Color(0xFFFBF8FD),
    surfaceDim = Color(0xFFDBD9DE),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F2F7),
    surfaceContainer = Color(0xFFEFEDF1),
    surfaceContainerHigh = Color(0xFFEAE7EC),
    surfaceContainerHighest = Color(0xFFE4E1E6),
    inverseSurface = Color(0xFF303036),
    inverseOnSurface = Color(0xFFF2EFF4),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C5D0),
)

internal val ImornDarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF22378F),
    onPrimaryContainer = Color(0xFFDEE0FF),
    inversePrimary = Indigo40,
    secondary = Apricot80,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Rose80,
    onTertiary = Color(0xFF561D35),
    tertiaryContainer = Color(0xFF72334B),
    onTertiaryContainer = Color(0xFFFFD9E3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF131318),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131318),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC6C5D0),
    surfaceTint = Indigo80,
    surfaceBright = Color(0xFF39383F),
    surfaceDim = Color(0xFF131318),
    surfaceContainerLowest = Color(0xFF0E0E13),
    surfaceContainerLow = Color(0xFF1B1B21),
    surfaceContainer = Color(0xFF1F1F25),
    surfaceContainerHigh = Color(0xFF2A292F),
    surfaceContainerHighest = Color(0xFF35343A),
    inverseSurface = Color(0xFFE4E1E9),
    inverseOnSurface = Color(0xFF303036),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF46464F),
)
