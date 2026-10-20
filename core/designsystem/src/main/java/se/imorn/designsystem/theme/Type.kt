package se.imorn.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import se.imorn.designsystem.R

// Imorn's typeface is Figtree (SIL Open Font License, licenses/Figtree-OFL.txt): friendly and clear, bundled as one
// variable font so it works offline and every weight comes from the same file. Screens never name it; they read
// ImornTheme.typography.
private fun figtree(weight: FontWeight) = Font(
    resId = R.font.figtree,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Figtree = FontFamily(
    figtree(FontWeight.Normal),
    figtree(FontWeight.Medium),
    figtree(FontWeight.SemiBold),
    figtree(FontWeight.Bold),
)

private fun TextStyle.withFigtree(weight: FontWeight? = fontWeight) = copy(fontFamily = Figtree, fontWeight = weight)

internal val ImornTypography = Typography().run {
    copy(
        displayLarge = displayLarge.withFigtree(FontWeight.Bold),
        displayMedium = displayMedium.withFigtree(FontWeight.Bold),
        displaySmall = displaySmall.withFigtree(FontWeight.Bold),
        headlineLarge = headlineLarge.withFigtree(FontWeight.Bold),
        headlineMedium = headlineMedium.withFigtree(FontWeight.SemiBold),
        headlineSmall = headlineSmall.withFigtree(FontWeight.SemiBold),
        titleLarge = titleLarge.withFigtree(FontWeight.SemiBold),
        titleMedium = titleMedium.withFigtree(FontWeight.SemiBold),
        titleSmall = titleSmall.withFigtree(FontWeight.SemiBold),
        bodyLarge = bodyLarge.withFigtree().copy(lineHeight = 24.sp),
        bodyMedium = bodyMedium.withFigtree(),
        bodySmall = bodySmall.withFigtree(),
        labelLarge = labelLarge.withFigtree(FontWeight.SemiBold),
        labelMedium = labelMedium.withFigtree(FontWeight.SemiBold),
        labelSmall = labelSmall.withFigtree(FontWeight.Medium),
    )
}
