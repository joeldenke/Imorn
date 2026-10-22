package se.imorn.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import se.imorn.model.PDF
import java.io.File
import java.util.UUID

/** Ways to bring something into a chapter. Each hands a `content://` Uri to the callbacks of [rememberCapture]. */
class Capture(
    /** The phone's camera app. Imorn asks for no camera permission: the camera app takes the picture. */
    val takePhoto: () -> Unit,
    /** The system Photo Picker: the user picks one picture, and Imorn never sees the rest of their photos. */
    val choosePhoto: () -> Unit,
    /** The system file picker, for a PDF or a picture of a paper. */
    val choosePaper: () -> Unit,
)

/**
 * The camera, the Photo Picker and the file picker for a navigation entry. None needs a permission. The Uri handed
 * to [onPhoto] or [onPaper] can be read for as long as the screen is open, so the ViewModel copies it right away
 * into the app's private storage (`ChapterRepository.keep`).
 *
 * Without a camera app, *Take a photo* opens the Photo Picker instead.
 */
@Composable
fun rememberCapture(onPhoto: (Uri) -> Unit, onPaper: (Uri) -> Unit): Capture {
    val context = LocalContext.current
    val photo by rememberUpdatedState(onPhoto)
    val paper by rememberUpdatedState(onPaper)
    // Survives the activity being recreated while the camera app is open.
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val uri = pending?.let(Uri::parse)
        pending = null
        if (taken && uri != null) photo(uri)
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { photo(it) }
    }
    val papers = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { paper(it) }
    }
    val choosePhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    return Capture(
        takePhoto = {
            val uri = newCaptureUri(context)
            pending = uri.toString()
            try {
                camera.launch(uri)
            } catch (e: ActivityNotFoundException) {
                pending = null
                choosePhoto()
            }
        },
        choosePhoto = choosePhoto,
        choosePaper = { papers.launch(arrayOf(PDF, "image/*")) },
    )
}

/** A new, empty file for the camera to write into. Earlier ones are kept already, so they are cleared first. */
private fun newCaptureUri(context: Context): Uri {
    val folder = File(context.cacheDir, "capture")
    folder.deleteRecursively()
    folder.mkdirs()
    val file = File(folder, "${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.capture", file)
}
