package se.imorn.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import se.imorn.database.ChapterArchive
import se.imorn.model.Chapter
import se.imorn.model.tomorrowPlan
import java.time.LocalDate
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent

/**
 * The home screen widget: the next step across every open chapter, with *Snabbfoto* and *Klart* right there, so
 * the day's one thing is a tap away without opening the app. Android wakes it outside any navigation entry, so it
 * reads the repository through [ChapterArchive]; with none published yet it draws the quiet empty state.
 */
class ImornWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ChapterArchive.repository()
        val item = repository?.let { nextStepOf(it.chapters.first(), LocalDate.now()) }

        provideContent {
            WidgetContent(
                chapterId = item?.planned?.chapterId,
                stepId = item?.planned?.step?.id,
                title = item?.planned?.step?.title,
                hasStep = item != null,
            )
        }
    }
}

/**
 * The one thing the widget shows: the first item of [tomorrowPlan] over the chapters, the same plan as the start
 * screen (it already keeps to active chapters), so the widget and the app never disagree. Deterministic: no model
 * picks it.
 */
internal fun nextStepOf(chapters: List<Chapter>, today: LocalDate) =
    tomorrowPlan(chapters, today).items.firstOrNull()

@Composable
fun WidgetContent(
    chapterId: String?,
    stepId: String?,
    title: String?,
    hasStep: Boolean,
) {
    val context = LocalContext.current
    val colors = widgetColors(context)
    // The widget is in a feature module and can't name :app's activity, so open the launcher intent instead.
    val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val rootModifier = GlanceModifier
        .fillMaxSize()
        .background(colors.background)
        .padding(12.dp)
    Column(
        modifier = if (openApp != null) {
            rootModifier.clickable(actionStartActivityIntent(openApp))
        } else {
            rootModifier
        },
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Text(
            text = context.getString(R.string.widget_app_name),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accent,
            ),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = if (hasStep && title != null) title else context.getString(R.string.widget_no_steps),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
            ),
            maxLines = 2,
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.End,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            val photoAction: Action = if (chapterId != null && stepId != null) {
                actionStartActivity(
                    WidgetPhotoActivity::class.java,
                    actionParametersOf(
                        WidgetPhotoActivity.CHAPTER_ID to chapterId,
                        WidgetPhotoActivity.STEP_ID to stepId,
                    ),
                )
            } else {
                actionStartActivity(WidgetPhotoActivity::class.java)
            }
            Text(
                text = context.getString(R.string.widget_quick_photo),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.accentAlt,
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(photoAction),
            )
            if (chapterId != null && stepId != null) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = context.getString(R.string.widget_done),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent,
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(
                            actionRunCallback<WidgetDoneActionCallback>(
                                actionParametersOf(
                                    WidgetDoneActionCallback.CHAPTER_ID to chapterId,
                                    WidgetDoneActionCallback.STEP_ID to stepId,
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

/**
 * The widget's colours, from the *Gryning* palette: indigo is the future, apricot the way, pink the memory. A
 * widget can't read a Compose theme, so the few roles it uses are resources, with a night set beside the day one;
 * [widgetColors] reads the resource that fits the phone right now (`values-night` when it is dark).
 */
internal fun widgetColors(context: Context): WidgetColors {
    fun role(resId: Int) = ColorProvider(Color(context.getColor(resId)))
    return WidgetColors(
        background = role(R.color.widget_background),
        textPrimary = role(R.color.widget_text_primary),
        accent = role(R.color.widget_accent),
        accentAlt = role(R.color.widget_accent_alt),
    )
}

internal data class WidgetColors(
    val background: ColorProvider,
    val textPrimary: ColorProvider,
    val accent: ColorProvider,
    val accentAlt: ColorProvider,
)
