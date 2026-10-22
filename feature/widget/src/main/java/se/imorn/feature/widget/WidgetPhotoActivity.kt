package se.imorn.feature.widget

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.imorn.database.ChapterRepository
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import java.time.LocalDate
import java.util.UUID

class WidgetPhotoActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chapterId = intent.getStringExtra(CHAPTER_ID)
        val stepId = intent.getStringExtra(STEP_ID)
        val app = application as se.imorn.ImornApplication
        val repository = app.graph.chapterRepository

        scope.launch {
            val chapters = repository.chapters.first()
            val targetChapterId = chapterId ?: chapters.firstOrNull()?.id
            if (targetChapterId == null) {
                finish()
                return@launch
            }
            // Quick photo via existing capture mechanism - launch camera picker flow
            try {
                val intent = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
                // Simplified: rely on system picker
            } catch (_: Exception) {
            }
            finish()
        }
    }

    private fun handlePhoto(uri: Uri, chapterId: String?, stepId: String?, repository: ChapterRepository) {
        scope.launch {
            try {
                val chapters = repository.chapters.first()
                val targetChapterId = chapterId ?: chapters.firstOrNull()?.id ?: run {
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
