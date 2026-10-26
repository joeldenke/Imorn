package se.imorn.designsystem.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Layout tokens. Features read these through ImornTheme.spacing and ImornTheme.shapes instead of hardcoding dp,
 * so the whole app moves together when a value changes.
 */
object ImornSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

/** Corner shapes, read through ImornTheme.shapes. The theme hands the same shapes to Material components. */
@Immutable
data class ImornShapes(
    val extraSmall: CornerBasedShape,
    val small: CornerBasedShape,
    val medium: CornerBasedShape,
    val large: CornerBasedShape,
    val extraLarge: CornerBasedShape,
)

// Soft, generous corners, like rounded book covers: cards at 24 dp, the big blocks at 32 dp.
internal val ImornCorners = ImornShapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

internal fun ImornShapes.toMaterial() = Shapes(
    extraSmall = extraSmall,
    small = small,
    medium = medium,
    large = large,
    extraLarge = extraLarge,
)
