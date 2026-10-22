package se.imorn.database

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.imorn.model.PDF
import java.io.File
import java.io.IOException
import kotlin.math.max

/** A file the app keeps: its name in the chapter's folder and its type. */
internal data class KeptFile(val name: String, val mimeType: String?)

/**
 * The photos and papers, in the app's private storage (`files/kept/<chapter>/`), which no other app can read and
 * backups leave out. Each chapter has its own folder, so deleting a chapter deletes its files in one go.
 *
 * Photos (and papers that are pictures) are stored again as JPEG, turned upright and at most [MAX_SIDE] pixels:
 * that drops what a camera writes into the file (where it was taken, the phone) and keeps the storage small. PDFs
 * are kept as they are. Each kept file gets a thumbnail for lists; one that can't be drawn simply has none.
 */
internal class KeptFiles(private val root: File, private val resolver: ContentResolver) {
    private fun folder(chapterId: String) = File(root, safe(chapterId))

    private fun thumbnails(chapterId: String) = File(folder(chapterId), THUMBNAILS)

    fun file(chapterId: String, name: String) = File(folder(chapterId), name)

    fun thumbnail(chapterId: String, name: String) = File(thumbnails(chapterId), "$name.jpg")

    /** The name the source has where it came from, such as `Lease.pdf`, if it says. */
    suspend fun displayName(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            resolver.query(source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    /** Copies [source] into the chapter's folder as entry [entryId]'s file, with a thumbnail. */
    suspend fun keep(chapterId: String, entryId: String, source: Uri, mimeType: String?): KeptFile =
        withContext(Dispatchers.IO) {
            val type = mimeType ?: resolver.getType(source)
            thumbnails(chapterId).mkdirs()
            val base = safe(entryId)
            val kept = if (type?.startsWith("image/") == true) keepImage(chapterId, base, source) else null
            kept ?: keepAsIs(chapterId, base, source, type)
        }

    private fun keepImage(chapterId: String, base: String, source: Uri): KeptFile? {
        val image = decodeUpright(source) ?: return null
        val name = "$base.jpg"
        return try {
            file(chapterId, name).outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
            writeThumbnail(image, thumbnail(chapterId, name))
            KeptFile(name, JPEG)
        } finally {
            image.recycle()
        }
    }

    private fun keepAsIs(chapterId: String, base: String, source: Uri, type: String?): KeptFile {
        val name = base + extension(type)
        val target = file(chapterId, name)
        val input = resolver.openInputStream(source) ?: throw IOException("Can't read $source")
        input.use { from -> target.outputStream().use { from.copyTo(it) } }
        if (type == PDF) pdfThumbnail(target, thumbnail(chapterId, name))
        return KeptFile(name, type)
    }

    /** The picture at [source], scaled down to at most [MAX_SIDE] pixels and turned the way the camera held it. */
    private fun decodeUpright(source: Uri): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return@runCatching null
        val rotation = resolver.openInputStream(source)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        val upright = scaled(decoded, MAX_SIDE, rotation)
        if (upright !== decoded) decoded.recycle()
        upright
    }.getOrNull()

    private fun writeThumbnail(image: Bitmap, target: File) {
        val small = scaled(image, THUMBNAIL_SIDE, 0f)
        target.outputStream().use { small.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        if (small !== image) small.recycle()
    }

    /** The first page of a PDF as its thumbnail, on white like paper. */
    private fun pdfThumbnail(pdf: File, target: File) {
        runCatching {
            ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount == 0) return@runCatching
                    renderer.openPage(0).use { page ->
                        val width = THUMBNAIL_SIDE
                        val height = (width.toLong() * page.height / max(page.width, 1)).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
                        bitmap.recycle()
                    }
                }
            }
        }
    }

    /** Deletes an entry's file and thumbnail. */
    suspend fun delete(chapterId: String, name: String) = withContext(Dispatchers.IO) {
        file(chapterId, name).delete()
        thumbnail(chapterId, name).delete()
    }

    /** Deletes every file kept in a chapter. */
    suspend fun deleteChapter(chapterId: String) = withContext(Dispatchers.IO) {
        folder(chapterId).deleteRecursively()
    }

    companion object {
        const val FOLDER = "kept"
        private const val THUMBNAILS = "thumbnails"
        private const val JPEG = "image/jpeg"
        const val MAX_SIDE = 2560
        const val THUMBNAIL_SIDE = 480
        private const val QUALITY = 88

        /** Ids become file names: anything but letters, digits, `-` and `_` is replaced. */
        fun safe(id: String) = id.replace(Regex("[^A-Za-z0-9_-]"), "_")

        private fun extension(type: String?) = when (type) {
            PDF -> ".pdf"
            "image/png" -> ".png"
            "image/jpeg" -> ".jpg"
            else -> ""
        }

        /** [image] turned by [degrees] and scaled to at most [side] pixels on its longest side; [image] is kept. */
        private fun scaled(image: Bitmap, side: Int, degrees: Float): Bitmap {
            val scale = minOf(1f, side.toFloat() / max(image.width, image.height))
            if (scale == 1f && degrees == 0f) return image
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postRotate(degrees)
            }
            return Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
    }
}
