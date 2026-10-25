package se.imorn.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.Dawn
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.designsystem.theme.Night

/*
 * The sky over the start screen: the dawn scene carried into everyday life. The same landscape as the app icon and
 * the welcome (a sky over still water, the sun and its reflection as narrowing bars), but as a header and coloured by
 * the hour: dawn in the morning, a high sun by day, a rose dusk in the evening and the moon at night. The top of the
 * sky is always dark, so its text is always light, and so are the status bar icons while it is behind them.
 */

/** The four skies of the day. */
enum class ImornSkyTime {
    /** 05–10: the sun rises out of the water, as in the app icon. */
    Morning,

    /** 10–17: the sun stands high over a soft horizon. */
    Day,

    /** 17–22: the sun goes down in rose and apricot. */
    Evening,

    /** 22–05: the moon and a few stars. */
    Night,
    ;

    companion object {
        /** The sky for [hour], 0 to 23, from the screen's ViewModel and never read from the clock in a composable. */
        fun at(hour: Int): ImornSkyTime = when (hour) {
            in MORNING_STARTS until DAY_STARTS -> Morning
            in DAY_STARTS until EVENING_STARTS -> Day
            in EVENING_STARTS until NIGHT_STARTS -> Evening
            else -> Night
        }

        private const val MORNING_STARTS = 5
        private const val DAY_STARTS = 10
        private const val EVENING_STARTS = 17
        private const val NIGHT_STARTS = 22
    }
}

private class Sky(val stops: List<Color>, val sun: Color, val glow: Color, val sunHeight: Float)

// The sky's own colors, from the brand's Night and Dawn. Only this file draws the sky, so they stay here.
private val Deep = Color(0xFF0A0F2C)
private val Dusk = Color(0xFF6E3B6E)
private val Rose = Color(0xFF9A5C7E)
private val Ember = Color(0xFFE07A5F)
private val DayBlue = Color(0xFF34479E)
private val Haze = Color(0xFFF6B98C)
private val NightLow = Color(0xFF2B2F6B)
private val SunCore = Color(0xFFFFE9CF)
private val Moon = Color(0xFFF3EEE4)
private val WaterTop = Color(0xFF34315E)
private val WaterBottom = Color(0xFF0A0F2C)
private val Star = Color.White.copy(alpha = 0.7f)

/** How high the sun stands over the horizon, in its own radii: half up at dawn and dusk, well up by day. */
private fun sky(time: ImornSkyTime) = when (time) {
    ImornSkyTime.Morning -> Sky(listOf(Deep, Night, Rose, Dawn), SunCore, Dawn, sunHeight = 0f)
    ImornSkyTime.Day -> Sky(listOf(Night, DayBlue, DayBlue, Haze), SunCore, Dawn, sunHeight = 1.6f)
    ImornSkyTime.Evening -> Sky(listOf(Deep, Dusk, Ember, Dawn), Dawn, Ember, sunHeight = 0.35f)
    ImornSkyTime.Night -> Sky(listOf(Deep, Deep, Night, NightLow), Moon, Moon, sunHeight = 2.2f)
}

/** The sun's place across the sky, as a share of the width: off to the side, so text never sits on it. */
private const val SUN_X = 0.8f

/** The reflection's bars, widest at the waterline, as a share of the sun's diameter (as in the app icon). */
private val ReflectionBars = listOf(1f, 0.7f, 0.45f, 0.25f)

/** Stars at night, as shares of the sky's width and height: fixed, so every screenshot has the same night. */
private val Stars = listOf(0.08f to 0.3f, 0.22f to 0.12f, 0.37f to 0.42f, 0.55f to 0.2f, 0.66f to 0.5f, 0.93f to 0.16f)

private val SunRadius = 26.dp
private val WaterHeight = 48.dp

/**
 * The sky at [hour] as the head of a screen, edge to edge from the top: [content] sits on it in light text
 * ([ImornSceneText], [ImornGlassPanel], [ImornSceneButton]), clear of the status bar. While the sky is behind the
 * status bar its icons are light; scrolled away, they are what they were.
 */
@Composable
fun ImornSkyHero(hour: Int, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val sky = sky(ImornSkyTime.at(hour))
    val night = ImornSkyTime.at(hour) == ImornSkyTime.Night
    val topInset = WindowInsets.safeDrawing.getTop(LocalDensity.current)
    var behindStatusBar by remember { mutableStateOf(true) }
    LightSystemBars(enabled = behindStatusBar, navigationBar = false)
    val corner = ImornTheme.spacing.xl
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { behindStatusBar = it.boundsInWindow().bottom > topInset }
            .clip(RoundedCornerShape(bottomStart = corner, bottomEnd = corner))
            .drawBehind { drawSky(sky, night) }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(
                start = ImornTheme.spacing.md,
                end = ImornTheme.spacing.md,
                top = ImornTheme.spacing.lg,
                bottom = ImornTheme.spacing.lg + WaterHeight / 2,
            ),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        content = content,
    )
}

private fun DrawScope.drawSky(sky: Sky, night: Boolean) {
    val water = WaterHeight.toPx()
    val horizon = size.height - water
    val radius = SunRadius.toPx()
    val sun = Offset(size.width * SUN_X, horizon - radius * sky.sunHeight)
    drawRect(
        brush = Brush.verticalGradient(sky.stops, endY = horizon),
        size = Size(size.width, horizon),
    )
    if (night) {
        Stars.forEach { (x, y) ->
            drawCircle(Star, radius = 1.5.dp.toPx(), center = Offset(size.width * x, horizon * y))
        }
    }
    val glow = radius * 6
    drawCircle(
        brush = Brush.radialGradient(listOf(sky.glow.copy(alpha = 0.45f), Color.Transparent), sun, glow),
        radius = glow,
        center = sun,
    )
    if (night) {
        // A crescent: the moon, with the night sky's color laid over most of it.
        drawCircle(Moon, radius = radius * 0.7f, center = sun)
        drawCircle(Night, radius = radius * 0.62f, center = sun + Offset(radius * 0.32f, -radius * 0.18f))
    } else {
        drawCircle(brush = Brush.radialGradient(listOf(SunCore, sky.sun), sun, radius), radius = radius, center = sun)
    }
    drawRect(
        brush = Brush.verticalGradient(listOf(WaterTop, WaterBottom), startY = horizon, endY = size.height),
        topLeft = Offset(0f, horizon),
        size = Size(size.width, water),
    )
    // The reflection: bars that narrow away from the waterline, as in the icon, faint at night.
    val bar = radius * 0.22f
    val strength = if (night) 0.4f else 0.85f
    ReflectionBars.forEachIndexed { index, share ->
        val width = radius * 2 * share
        drawRoundRect(
            color = sky.glow.copy(alpha = strength - index * 0.16f),
            topLeft = Offset(sun.x - width / 2, horizon + bar * 0.8f + index * bar * 2f),
            size = Size(width, bar),
            cornerRadius = CornerRadius(bar / 2),
        )
    }
}
