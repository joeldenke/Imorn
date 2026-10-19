package se.imorn.ui

/**
 * The one person all previews show, so screenshots are comparable and reviewable (the same idea as Novila's
 * PreviewData in :core:ui). Feature previews read this instead of inventing their own sample content.
 */
object PreviewData {
    const val NAME = "Alex"
    const val PROJECT = "Moving home"
    const val NEXT_STEP = "Book the moving van"
    const val ASSISTANT_MESSAGE =
        "You are three steps in. Booking the van next keeps the rest of the week on track."
}
