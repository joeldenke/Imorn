package se.imorn.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
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

// Corner radius from the base 14 dp, as in Novila's "Skog & sol" language.
internal val ImornShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
