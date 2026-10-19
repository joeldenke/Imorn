package se.imorn.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Imorn's palette. A calm, local-first tool: a deep pine primary, a warm sand accent.
// Every value here is referenced through ImornTheme.colors, never hardcoded in features.

val Pine40 = Color(0xFF2F5D50)
val Pine80 = Color(0xFF9CD3BE)
val Sand40 = Color(0xFF7A5B2E)
val Sand80 = Color(0xFFE8C48A)
val Clay40 = Color(0xFF7C4E43)
val Clay80 = Color(0xFFF0B7A8)

internal val ImornLightColors = lightColorScheme(
    primary = Pine40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4EED6),
    onPrimaryContainer = Color(0xFF002016),
    secondary = Sand40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB0),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Clay40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF31120C),
    background = Color(0xFFFAFDF9),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFAFDF9),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404943),
    outline = Color(0xFF707973),
)

internal val ImornDarkColors = darkColorScheme(
    primary = Pine80,
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF14513F),
    onPrimaryContainer = Color(0xFFB4EED6),
    secondary = Sand80,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF614200),
    onSecondaryContainer = Color(0xFFFFDDB0),
    tertiary = Clay80,
    onTertiary = Color(0xFF4A2820),
    tertiaryContainer = Color(0xFF653E34),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outline = Color(0xFF89938C),
)
