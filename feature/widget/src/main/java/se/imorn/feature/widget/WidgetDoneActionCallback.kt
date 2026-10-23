package se.imorn.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.first
import se.imorn.database.ChapterArchive
import se.imorn.model.complete
import java.time.LocalDate

/** *Klart* on the widget: the step falls through the now-line, and the widget redraws to the next one. */
class WidgetDoneActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val chapterId = parameters[CHAPTER_ID] ?: return
        val stepId = parameters[STEP_ID] ?: return
        val repository = ChapterArchive.repository() ?: return
        val chapter = repository.chapter(chapterId).first() ?: return
        repository.save(chapter.complete(stepId, LocalDate.now()))
        ImornWidget().update(context, glanceId)
    }

    companion object {
        val CHAPTER_ID = ActionParameters.Key<String>("chapter_id")
        val STEP_ID = ActionParameters.Key<String>("step_id")
    }
}
