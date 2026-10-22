package se.imorn.feature.widget
import se.imorn.ImornApplication

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
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
import se.imorn.database.ChapterRepository
import se.imorn.model.ChapterState
import se.imorn.model.tomorrowPlan
import java.time.LocalDate

class ImornWidget(private val repository: ChapterRepository) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val chapters = repository.chapters.first().filter { it.state != ChapterState.Closed }
        val today = LocalDate.now()
        val plan = tomorrowPlan(chapters, today)
        val item = plan.items.firstOrNull()

        provideContent {
            WidgetContent(
                chapterId = item?.planned?.chapterId,
                stepId = item?.planned?.step?.id,
                title = item?.planned?.step?.title,
                hasStep = item != null,
            )
        }
    }

    companion object {
        fun create(context: Context): ImornWidget {
            val app = context.applicationContext
            val imornApp = app as ImornApplication
            val graph = imornApp.graph
            val repo = graph.chapterRepository
            return ImornWidget(repo)
        }
    }
    }
}

@Composable
fun WidgetContent(
    chapterId: String?,
    stepId: String?,
    title: String?,
    hasStep: Boolean,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(12.dp)
            .clickable(actionStartActivity(se.imorn.app.MainActivity::class.java)),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Text(
                text = "Imorn",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = WidgetColors.accent,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        if (hasStep && title != null) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WidgetColors.textPrimary,
                ),
                maxLines = 2,
            )
        } else {
            Text(
                text = "Inget planerat än",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = WidgetColors.textPrimary,
                ),
                maxLines = 2,
            )
        }
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.End,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            val photoAction: Action = if (chapterId != null && stepId != null) {
                actionStartActivity(
                    WidgetPhotoActivity::class.java,
                    androidx.glance.action.actionParametersOf(
                        WidgetPhotoActivity.CHAPTER_ID to chapterId,
                        WidgetPhotoActivity.STEP_ID to stepId,
                    ),
                )
            } else {
                actionStartActivity(WidgetPhotoActivity::class.java)
            }
            Text(
                text = "Snabbfoto",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = WidgetColors.accentAlt,
                ),
                modifier = GlanceModifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(photoAction),
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            if (chapterId != null && stepId != null) {
                Text(
                    text = "Klart",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WidgetColors.accent,
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable(
                            actionRunCallback<WidgetDoneActionCallback>(
                                androidx.glance.action.actionParametersOf(
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

object WidgetColors {
    val background = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFFF7F5FF),
        night = androidx.compose.ui.graphics.Color(0xFF151226),
    )
    val textPrimary = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFF1F1B2E),
        night = androidx.compose.ui.graphics.Color(0xFFF5F3FF),
    )
    val accent = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFF5B4BFF),
        night = androidx.compose.ui.graphics.Color(0xFFB9B0FF),
    )
    val accentAlt = ColorProvider(
        day = androidx.compose.ui.graphics.Color(0xFFFF7A3E),
        night = androidx.compose.ui.graphics.Color(0xFFFF9C6B),
    )
}
