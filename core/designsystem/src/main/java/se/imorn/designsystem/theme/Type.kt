package se.imorn.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import se.imorn.designsystem.R

/** Imorn's type scale, read through ImornTheme.typography. The theme hands the same styles to Material components. */
@Immutable
data class ImornTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

// Imorn's typeface is Bricolage Grotesque (SIL Open Font License, licenses/BricolageGrotesque-OFL.txt): warm, a
// little playful, and clear at every size. It is bundled as one variable font, so it works offline and every weight
// and optical size comes from the same file. Headings use the optical size for display, set tight; text uses the
// one for small sizes. Screens never name it; they read ImornTheme.typography.
private fun bricolage(weight: FontWeight, opticalSize: Float) = Font(
    resId = R.font.bricolage_grotesque,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", opticalSize),
    ),
)

private fun bricolageFamily(opticalSize: Float) = FontFamily(
    bricolage(FontWeight.Normal, opticalSize),
    bricolage(FontWeight.Medium, opticalSize),
    bricolage(FontWeight.SemiBold, opticalSize),
    bricolage(FontWeight.Bold, opticalSize),
    bricolage(FontWeight.ExtraBold, opticalSize),
)

private const val DISPLAY_OPTICAL_SIZE = 48f
private const val TEXT_OPTICAL_SIZE = 14f

private val Display = bricolageFamily(DISPLAY_OPTICAL_SIZE)
private val Text = bricolageFamily(TEXT_OPTICAL_SIZE)

private fun style(family: FontFamily, weight: FontWeight, size: Int, lineHeight: Int, tracking: Float = 0f) =
    TextStyle(
        fontFamily = family,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = tracking.em,
    )

// Large and bold at the top, calm and roomy for reading: the scale is Imorn's own, sized for big text.
internal val ImornTextStyles = ImornTypography(
    displayLarge = style(Display, FontWeight.ExtraBold, size = 56, lineHeight = 58, tracking = -0.04f),
    displayMedium = style(Display, FontWeight.ExtraBold, size = 44, lineHeight = 46, tracking = -0.035f),
    displaySmall = style(Display, FontWeight.ExtraBold, size = 36, lineHeight = 38, tracking = -0.03f),
    headlineLarge = style(Display, FontWeight.Bold, size = 34, lineHeight = 36, tracking = -0.03f),
    headlineMedium = style(Display, FontWeight.Bold, size = 30, lineHeight = 33, tracking = -0.025f),
    headlineSmall = style(Display, FontWeight.Bold, size = 26, lineHeight = 29, tracking = -0.02f),
    titleLarge = style(Display, FontWeight.Bold, size = 22, lineHeight = 27, tracking = -0.01f),
    titleMedium = style(Text, FontWeight.SemiBold, size = 17, lineHeight = 23),
    titleSmall = style(Text, FontWeight.SemiBold, size = 15, lineHeight = 20),
    bodyLarge = style(Text, FontWeight.Normal, size = 17, lineHeight = 25),
    bodyMedium = style(Text, FontWeight.Normal, size = 15, lineHeight = 21),
    bodySmall = style(Text, FontWeight.Normal, size = 13, lineHeight = 18),
    labelLarge = style(Text, FontWeight.SemiBold, size = 15, lineHeight = 20),
    labelMedium = style(Text, FontWeight.SemiBold, size = 13, lineHeight = 17),
    labelSmall = style(Text, FontWeight.SemiBold, size = 11, lineHeight = 14, tracking = 0.08f),
)

internal fun ImornTypography.toMaterial() = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)
