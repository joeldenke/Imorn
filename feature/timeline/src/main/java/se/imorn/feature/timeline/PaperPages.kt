package se.imorn.feature.timeline

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import se.imorn.designsystem.theme.ImornTheme
import java.io.File
import kotlin.math.max

/**
 * A PDF kept in the app, drawn on the phone with Android's own PDF renderer: the paper never goes to another app.
 * One page is drawn at a time (the renderer allows no more), and only the pages on screen are held in memory.
 */
internal class PaperDocument private constructor(
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
) {
    private val lock = Mutex()
    val pageCount: Int = renderer.pageCount

    /** Page [index] (from 0) drawn [width] pixels wide on white, as paper. */
    suspend fun render(index: Int, width: Int): Bitmap = lock.withLock {
        withContext(Dispatchers.IO) {
            renderer.openPage(index).use { page ->
                val pixels = width.coerceIn(1, MAX_WIDTH)
                val height = (pixels.toLong() * page.height / max(page.width, 1)).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(pixels, height, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.WHITE)
                    page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }
    }

    /** Closes the file once any page being drawn is done. */
    fun close() {
        closing.launch {
            lock.withLock {
                renderer.close()
                descriptor.close()
            }
        }
    }

    companion object {
        private const val MAX_WIDTH = 1600
        private val closing = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /** Opens [path], or null when it isn't a PDF the renderer can read. */
        suspend fun open(path: String): PaperDocument? = withContext(Dispatchers.IO) {
            runCatching {
                val descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                try {
                    PaperDocument(descriptor, PdfRenderer(descriptor))
                } catch (e: Exception) {
                    descriptor.close()
                    throw e
                }
            }.getOrNull()
        }
    }
}

/** A kept PDF as its screen sees it: still opening, open, or not one the phone can read. */
internal sealed interface Paper {
    data object Opening : Paper

    data class Open(val document: PaperDocument) : Paper

    data object Unreadable : Paper
}

/** The PDF at [path], open for as long as the screen shows it. */
@Composable
internal fun rememberPaper(path: String?): Paper {
    val paper by produceState<Paper>(Paper.Opening, path) {
        val opened = path?.let { PaperDocument.open(it) }
        value = opened?.let { Paper.Open(it) } ?: Paper.Unreadable
        awaitDispose { opened?.close() }
    }
    return paper
}

/** Page [index] of [document], as wide as there is room for. A white sheet until it is drawn. */
@Composable
internal fun PaperPage(document: PaperDocument, index: Int, contentDescription: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val width = with(LocalDensity.current) { maxWidth.roundToPx() }
        val page by produceState<ImageBitmap?>(null, document, index, width) {
            value = runCatching { document.render(index, width).asImageBitmap() }.getOrNull()
        }
        val current = page
        if (current == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(A4)
                    .background(ImornTheme.surfaces.card),
            )
        } else {
            Image(
                bitmap = current,
                contentDescription = contentDescription,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Width over height of an A4 sheet, the shape a page has until it is drawn. */
private const val A4 = 210f / 297f
