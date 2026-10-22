package se.imorn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import se.imorn.designsystem.component.ImornConfirmDialog

/**
 * Asked once, right after a step with a photo prompt is done, such as "Photograph the meter readings". *Not now* is
 * a fine answer: the step is done either way.
 */
@Composable
fun PhotoPromptDialog(prompt: String, onTakePhoto: () -> Unit, onDismiss: () -> Unit) {
    ImornConfirmDialog(
        title = stringResource(R.string.photo_prompt_title),
        text = prompt,
        confirmText = stringResource(R.string.photo_prompt_take),
        dismissText = stringResource(R.string.photo_prompt_not_now),
        onConfirm = onTakePhoto,
        onDismiss = onDismiss,
    )
}
