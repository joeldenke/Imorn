package se.imorn.feature.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.imorn.database.ChapterRepository
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.ui.rememberCapture
import java.time.LocalDate
import java.util.UUID

class WidgetPhotoActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chapterId = intent.getStringExtra(CHAPTER_ID)
        val stepId = intent.getStringExtra(STEP_ID)
        val app = application as se.imorn.ImornApplication
        val repository = (app.graph as se.imorn.di.AppGraph).chapterRepository

        setContent {
            val capture = rememberCapture(
                onPhoto = { uri ->
                    handlePhoto(uri, chapterId, stepId, repository)
                },
                onPaper = { uri ->
                    handlePhoto(uri, chapterId, stepId, repository)
                },
            )
            LaunchedEffect(Unit) {
                capture.takePhoto()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {}
        }
    }

    private fun handlePhoto(uri: android.net.Uri, chapterId: String?, stepId: String?, repository: ChapterRepository) {
        scope.launch {
            try {
                val targetChapterId = chapterId ?: repository.chapters.first().firstOrNull()?.id ?: run {
                    finish()
                    return@launch
                }
                val entry = Entry(
                    id = UUID.randomUUID().toString(),
                    chapterId = targetChapterId,
                    kind = EntryKind.Photo,
                    on = LocalDate.now(),
                    stepId = stepId,
                )
                repository.keepIfReadable(entry, uri)
            } finally {
                finish()
            }
        }
    }

    companion object {
        const val CHAPTER_ID = "chapter_id"
        const val STEP_ID = "step_id"
    }
}
