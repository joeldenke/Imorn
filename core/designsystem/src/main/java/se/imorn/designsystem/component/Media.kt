package se.imorn.designsystem.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.imorn.designsystem.theme.ImornTheme
import kotlin.math.max

/** The screen's one floating action, such as the quick + on a chapter. */
@Composable
fun ImornFloatingButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = ImornTheme.shapes.large,
        containerColor = ImornTheme.colors.primary,
        contentColor = ImornTheme.colors.onPrimary,
    ) {
        ImornIcon(icon = icon, contentDescription = contentDescription, tint = ImornTheme.colors.onPrimary)
    }
}

/** A sheet from the bottom of the screen, such as the quick +'s choices. Swiping it down or tapping outside closes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImornSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ImornTheme.surfaces.card,
        contentColor = ImornTheme.colors.onSurface,
        content = {
            content()
        },
    )
}

/** One choice in an [ImornSheet]: an icon and a word, big enough for the thumb. */
@Composable
fun ImornSheetAction(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ImornTheme.spacing.lg, vertical = ImornTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        ImornIcon(icon = icon, contentDescription = null, tint = ImornTheme.colors.primary)
        ImornText(text = text, style = ImornTheme.typography.bodyLarge)
    }
}

/**
 * A picture from a file the app keeps, read off the main thread and scaled down to at most [maxSide] pixels. Until
 * it is read, or when there is none (as in previews), it is a quiet tile in the color of memories. [crop] fills the
 * space, as thumbnails do; otherwise the whole picture shows.
 */
@Composable
fun ImornImage(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    crop: Boolean = true,
    maxSide: Int = THUMBNAIL_SIDE,
) {
    val image by produceState<ImageBitmap?>(null, path, maxSide) {
        value = path?.let { withContext(Dispatchers.IO) { decode(it, maxSide)?.asImageBitmap() } }
    }
    val shape = ImornTheme.shapes.small
    val current = image
    if (current == null) {
        // Still a picture to TalkBack while it loads, or when the file is missing.
        val described = if (contentDescription != null) {
            Modifier.semantics { this.contentDescription = contentDescription }
        } else {
            Modifier
        }
        Box(modifier.clip(shape).background(ImornTheme.colors.tertiaryContainer).then(described))
    } else {
        Image(
            bitmap = current,
            contentDescription = contentDescription,
            contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
            modifier = modifier.clip(shape),
        )
    }
}

/** The longest side a full picture is shown at: sharp on a phone, without holding a camera's full size in memory. */
const val FULL_IMAGE_SIDE = 2048
private const val THUMBNAIL_SIDE = 480

private fun decode(path: String, maxSide: Int): Bitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0) return@runCatching null
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
}.getOrNull()
