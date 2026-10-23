package se.imorn.feature.widget

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.glance.action.ActionParameters
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.imorn.database.ChapterArchive
import se.imorn.model.Entry
import se.imorn.model.EntryKind
import se.imorn.ui.newCaptureUriFor
import java.time.LocalDate
import java.util.UUID

/**
 * The widget's *Snabbfoto*: opens the camera app for one picture and keeps it in the chapter the widget showed.
 * It needs no camera permission — the camera app takes the picture, as in [se.imorn.ui.rememberCapture] — and, like
 * the widget itself, it runs outside a navigation entry, so it finds the repository through [ChapterArchive].
 * The photo lands in the step's entry, or the chapter's, and the widget redraws when it is kept.
 */
class WidgetPhotoActivity : ComponentActivity() {
    private var pending: Uri? = null
    private var chapterId: String? = null
    private var stepId: String? = null

    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val uri = pending
        pending = null
        if (taken && uri != null) keepPhoto(uri) else finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chapterId = intent.getStringExtra(CHAPTER_ID.name)
        stepId = intent.getStringExtra(STEP_ID.name)
        if (savedInstanceState != null) return
        // No chapter yet (the widget had none): open the app so the user can start one.
        if (chapterId == null) {
            finish()
            return
        }
        val uri = newCaptureUriFor(this)
        pending = uri
        camera.launch(uri)
    }

    private fun keepPhoto(uri: Uri) {
        val chapter = chapterId
        val step = stepId
        lifecycleScope.launch {
            val repository = ChapterArchive.repository()
            if (repository != null && chapter != null) {
                val entry = Entry(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter,
                    kind = EntryKind.Photo,
                    on = LocalDate.now(),
                    stepId = step,
                )
                withContext(Dispatchers.IO) { repository.keepIfReadable(entry, uri) }
                ImornWidget().updateAll(this@WidgetPhotoActivity)
            }
            finish()
        }
    }

    companion object {
        val CHAPTER_ID = ActionParameters.Key<String>("chapter_id")
        val STEP_ID = ActionParameters.Key<String>("step_id")
    }
}
