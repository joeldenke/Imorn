package se.imorn.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import kotlinx.coroutines.flow.first
import se.imorn.model.complete
import se.imorn.model.withStepDate
import java.time.LocalDate

class WidgetDoneActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val chapterId = parameters[CHAPTER_ID] ?: return
        val stepId = parameters[STEP_ID] ?: return
        val app = context.applicationContext as se.imorn.ImornApplication
        val repository = app.graph.chapterRepository
        val chapters = repository.chapters.first()
        val chapter = chapters.firstOrNull { it.id == chapterId } ?: return
        repository.save(chapter.complete(stepId, LocalDate.now()))
        ImornWidget(repository).update(context, glanceId)
    }

    companion object {
        val CHAPTER_ID = ActionParameters.Key<String>("chapter_id")
        val STEP_ID = ActionParameters.Key<String>("step_id")
    }
}

class WidgetTomorrowActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val chapterId = parameters[CHAPTER_ID] ?: return
        val stepId = parameters[STEP_ID] ?: return
        val app = context.applicationContext as se.imorn.ImornApplication
        val repository = app.graph.chapterRepository
        val chapters = repository.chapters.first()
        val chapter = chapters.firstOrNull { it.id == chapterId } ?: return
        repository.save(chapter.withStepDate(stepId, LocalDate.now().plusDays(1)))
        ImornWidget(repository).update(context, glanceId)
    }

    companion object {
        val CHAPTER_ID = ActionParameters.Key<String>("chapter_id")
        val STEP_ID = ActionParameters.Key<String>("step_id")
    }
}
