package se.imorn.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

internal val ImornLightColors = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF00115A),
    secondary = Apricot40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = Rose40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E3),
    onTertiaryContainer = Color(0xFF3A0720),
    background = Color(0xFFFBF8FD),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FD),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C5D0),
)

internal val ImornDarkColors = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF22378F),
    onPrimaryContainer = Color(0xFFDEE0FF),
    secondary = Apricot80,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Rose80,
    onTertiary = Color(0xFF561D35),
    tertiaryContainer = Color(0xFF72334B),
    onTertiaryContainer = Color(0xFFFFD9E3),
    background = Color(0xFF131318),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131318),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC6C5D0),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF46464F),
)
