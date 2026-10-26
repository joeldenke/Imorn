package se.imorn.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/**
 * The few icons Imorn uses, drawn as vectors here (paths from Material Symbols, Apache 2.0, and the templates' own line
 * icons), so the app needs no icon library. Icons are always paired with a word on screen; alone, they get a
 * `contentDescription`.
 */
object ImornIcons {
    val Add: ImageVector = icon("add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

    val Camera: ImageVector = icon(
        "camera",
        "M9,2L7.17,4H4c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6" +
            "c0,-1.1 -0.9,-2 -2,-2h-3.17L15,2H9z" +
            "M12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5z" +
            "M12,15.2c1.77,0 3.2,-1.43 3.2,-3.2S13.77,8.8 12,8.8 8.8,10.23 8.8,12s1.43,3.2 3.2,3.2z",
    )

    val Photo: ImageVector = icon(
        "photo",
        "M21,19V5c0,-1.1 -0.9,-2 -2,-2H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2z" +
            "M8.5,13.5l2.5,3.01L14.5,12l4.5,6H5l3.5,-4.5z",
    )

    val Note: ImageVector = icon(
        "note",
        "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25z" +
            "M20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 " +
            "3.75,3.75 1.83,-1.83z",
    )

    val Step: ImageVector = icon(
        "step",
        "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z" +
            "M10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z",
    )

    val Paper: ImageVector = icon(
        "paper",
        "M14,2H6c-1.1,0 -1.99,0.9 -1.99,2L4,20c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8l-6,-6z" +
            "M16,18H8v-2h8v2zM16,14H8v-2h8v2zM13,9V3.5L18.5,9H13z",
    )

    val Lock: ImageVector = icon(
        "lock",
        "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 " +
            "2,-2V10c0,-1.1 -0.9,-2 -2,-2z" +
            "M12,17c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z" +
            "M15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1v2z",
    )

    val Close: ImageVector = icon(
        "close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
    )

    val Keep: ImageVector = icon(
        "keep",
        "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z" +
            "M10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z",
    )

    val Check: ImageVector = icon("check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")

    val More: ImageVector = icon(
        "more",
        "M6,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" +
            "M18,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" +
            "M12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
    )

    val Delete: ImageVector = icon(
        "delete",
        "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z",
    )

    val Forward: ImageVector = icon("forward", "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z")

    val Calendar: ImageVector = icon(
        "calendar",
        "M17,12h-5v5h5v-5zM16,1v2H8V1H6v2H5c-1.11,0 -1.99,0.9 -1.99,2L3,19c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5" +
            "c0,-1.1 -0.9,-2 -2,-2h-1V1h-2zM19,19H5V8h14v11z",
    )

    // The templates' icons, named in content/ (a template's "icon"): drawn here as soft line icons, so a chapter is
    // recognised at a glance. An unknown name gets a plain circle.
    private val templateIcons: Map<String, ImageVector> = mapOf(
        "home" to line("home", "M4,11l8,-7 8,7M6,9.5V20h12V9.5M10,20v-5h4v5"),
        "key" to line("key", "M3,14a4,4 0 1,0 8,0a4,4 0 1,0 -8,0zM11,14h10M18,14v3M21,14v2"),
        "sign" to line("sign", "M6,3v18M6,6h13v8H6M10,10h5"),
        "sprout" to line("sprout", "M12,21v-9M12,12c0,-4 3,-7 8,-7 0,4 -3,7 -8,7zM12,14c-4,0 -7,-3 -7,-7 4,0 7,3 7,7z"),
        "hammer" to line("hammer", "M4,5h9v4H4zM8.5,9v12M13,6h3l3,3"),
        "heart" to line(
            "heart",
            "M12,20C12,20 5,15.5 5,10a3.5,3.5 0 0,1 7,-1.5a3.5,3.5 0 0,1 7,1.5c0,5.5 -7,10 -7,10z",
        ),
        "rings" to line("rings", "M3,14a5,5 0 1,0 10,0a5,5 0 1,0 -10,0zM11,10a5,5 0 1,0 10,0a5,5 0 1,0 -10,0z"),
        "book" to line("book", "M5,4h10a3,3 0 0,1 3,3v13H8a3,3 0 0,1 -3,-3zM5,17a3,3 0 0,1 3,-3h10"),
        "road" to line("road", "M8,3L4,21M16,3l4,18M12,4v3M12,10v3M12,16v3"),
        "car" to line("car", "M5,16v-4l2,-5h10l2,5v4zM5,16v2M19,16v2M5,12h14M8,14h0.01M16,14h0.01"),
        "briefcase" to line("briefcase", "M4,8h16v11H4zM9,8V5h6v3M4,13h16"),
        "sun" to line(
            "sun",
            "M8,12a4,4 0 1,0 8,0a4,4 0 1,0 -8,0zM12,2v2M12,20v2M2,12h2M20,12h2M4.9,4.9l1.4,1.4M17.7,17.7l1.4,1.4" +
                "M4.9,19.1l1.4,-1.4M17.7,6.3l1.4,-1.4",
        ),
        "globe" to line(
            "globe",
            "M3,12a9,9 0 1,0 18,0a9,9 0 1,0 -18,0zM3,12h18M12,3c2.5,2.5 3.8,5.5 3.8,9s-1.3,6.5 -3.8,9" +
                "M12,3c-2.5,2.5 -3.8,5.5 -3.8,9s1.3,6.5 3.8,9",
        ),
    )

    private val plainMark = line("plain", "M7,12a5,5 0 1,0 10,0a5,5 0 1,0 -10,0z")

    /** The icon a template names, such as `home` for moving, or a plain mark for an own chapter. */
    fun template(name: String?): ImageVector = name?.let(templateIcons::get) ?: plainMark

    private fun line(name: String, path: String) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(path),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = LINE_WIDTH,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()

    private const val LINE_WIDTH = 1.8f

    private fun icon(name: String, path: String) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black)).build()
}

/** An icon from [ImornIcons]. [contentDescription] is null when a word beside it already says what it is. */
@Composable
fun ImornIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = ImornTheme.colors.onSurfaceVariant,
) {
    Icon(imageVector = icon, contentDescription = contentDescription, modifier = modifier, tint = tint)
}
