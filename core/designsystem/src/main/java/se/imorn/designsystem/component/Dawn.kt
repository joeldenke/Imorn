package se.imorn.designsystem.component

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import se.imorn.designsystem.theme.Dawn
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.designsystem.theme.Night

/*
 * The dawn scene: Imorn's brand moment, for the welcome and other first impressions. The app icon told as a
 * landscape: a night sky that warms towards the horizon, the sun rising out of still water, and its reflection as
 * bars that narrow downwards. Content sits on it in glass panels that blur the scene behind them (Haze, on
 * Android 12 and newer; a translucent tint on older phones). The scene is always dark, whatever the theme, so its
 * text is always light and the status bar icons with it.
 */

// The scene's own colors, from the brand's Night and Dawn. Only this file draws the scene, so they stay here.
private val SkyTop = Color(0xFF0A0F2C)
private val SkyRose = Color(0xFF9A5C7E)
private val SunCore = Color(0xFFFFE9CF)
private val WaterTop = Color(0xFF34315E)
private val WaterBottom = Color(0xFF0A0F2C)
private val OnScene = Color.White
private val OnSceneMuted = Color.White.copy(alpha = 0.78f)
private val GlassTint = Color.White.copy(alpha = 0.10f)
private val GlassOutline = Color.White.copy(alpha = 0.22f)
private val GlassFallback = Night.copy(alpha = 0.72f)

/** Where the horizon is, as a share of the scene's height. */
private const val HORIZON = 0.56f

/** The sun's radius as a share of the scene's width. */
private const val SUN = 0.16f

/** The reflection's bars, widest at the waterline, as a share of the sun's diameter (as in the app icon). */
private val ReflectionBars = listOf(1f, 0.78f, 0.58f, 0.4f, 0.24f)

/** The glass panels under a scene blur what the scene captured; elsewhere they fall back to a tint. */
private val LocalSceneHaze = staticCompositionLocalOf<HazeState?> { null }

/**
 * The dawn scene behind [content], filling the screen edge to edge; [content] takes care of the system bars itself
 * (`WindowInsets.safeDrawing`). While it shows, the status bar icons are light.
 */
@Composable
fun ImornDawnScene(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val haze = rememberHazeState()
    LightSystemBars()
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .hazeSource(haze)
                .drawBehind { drawDawn() },
        )
        CompositionLocalProvider(LocalSceneHaze provides haze) {
            content()
        }
    }
}

private fun DrawScope.drawDawn() {
    val horizon = size.height * HORIZON
    val sun = size.width * SUN
    val center = Offset(size.width / 2, horizon)
    // The sky, from the night at the top to the warm horizon.
    drawRect(
        brush = Brush.verticalGradient(
            0f to SkyTop,
            0.42f to Night,
            0.82f to SkyRose,
            1f to Dawn,
            endY = horizon,
        ),
        size = Size(size.width, horizon),
    )
    // The glow around the sun, then the sun itself; the water covers its lower half.
    drawCircle(
        brush = Brush.radialGradient(listOf(Dawn.copy(alpha = 0.6f), Color.Transparent), center, size.width * 0.7f),
        radius = size.width * 0.7f,
        center = center,
    )
    drawCircle(brush = Brush.radialGradient(listOf(SunCore, Dawn), center, sun), radius = sun, center = center)
    drawRect(
        brush = Brush.verticalGradient(listOf(WaterTop, WaterBottom), startY = horizon, endY = size.height),
        topLeft = Offset(0f, horizon),
        size = Size(size.width, size.height - horizon),
    )
    // The reflection: bars that narrow away from the waterline, as in the icon.
    val bar = sun * 0.18f
    ReflectionBars.forEachIndexed { index, share ->
        val width = sun * 2 * share
        drawRoundRect(
            color = Dawn.copy(alpha = 0.85f - index * 0.14f),
            topLeft = Offset(center.x - width / 2, horizon + bar * 0.6f + index * bar * 1.9f),
            size = Size(width, bar),
            cornerRadius = CornerRadius(bar / 2),
        )
    }
}

/** Light status and navigation bar icons while the scene shows, back to what they were when it leaves. */
@Composable
private fun LightSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val status = controller?.isAppearanceLightStatusBars
        val navigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (status != null) controller?.isAppearanceLightStatusBars = status
            if (navigation != null) controller?.isAppearanceLightNavigationBars = navigation
        }
    }
}

/**
 * A pane of frosted glass on the dawn scene: the scene shows through, blurred, with a hairline edge. [selected]
 * marks it with the sun's color, for choices; with [onClick] the whole panel is the target and reads as a checkbox
 * to TalkBack.
 */
@Composable
fun ImornGlassPanel(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = ImornTheme.shapes.large
    val haze = LocalSceneHaze.current
    val glass = if (haze != null) {
        Modifier.hazeBlur(
            input = HazeInput.Sources(haze),
            style = HazeBlurStyle {
                blurRadius(28.dp)
                noiseFactor(0.08f)
                colorEffects(listOf(HazeColorEffect.tint(if (selected) Dawn.copy(alpha = 0.18f) else GlassTint)))
                fallbackColorEffect(HazeColorEffect.tint(GlassFallback))
            },
        )
    } else {
        Modifier.background(GlassFallback)
    }
    val clickable = if (onClick != null) {
        Modifier
            .semantics { this.selected = selected }
            .clickable(role = Role.Checkbox, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .clip(shape)
            .then(glass)
            .border(if (selected) 2.dp else 1.dp, if (selected) Dawn else GlassOutline, shape)
            .then(clickable)
            .padding(ImornTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
        content = content,
    )
}

/** Text on the dawn scene: white, or softened when [muted]. */
@Composable
fun ImornSceneText(text: String, style: TextStyle, modifier: Modifier = Modifier, muted: Boolean = false) {
    Text(text = text, style = style, color = if (muted) OnSceneMuted else OnScene, modifier = modifier)
}

/** The scene's one main action: the sun's color on a pill, easy to find with the thumb. */
@Composable
fun ImornSceneButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(Dawn)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ImornTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ImornTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Night,
        )
    }
}

/** A quiet action on the scene, such as *Skip*. */
@Composable
fun ImornSceneTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ImornTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = ImornTheme.typography.labelLarge, color = OnSceneMuted)
    }
}

/**
 * Where in a series of pages the user is: the current page as a short sun-colored bar, the others as dots.
 * [description] is what TalkBack reads, such as "Page 2 of 3".
 */
@Composable
fun ImornPageIndicator(count: Int, current: Int, description: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { page ->
            Box(
                Modifier
                    .size(height = 8.dp, width = if (page == current) 24.dp else 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (page == current) Dawn else GlassOutline),
            )
        }
    }
}
