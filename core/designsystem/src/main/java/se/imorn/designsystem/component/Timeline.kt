package se.imorn.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme

/**
 * The now-line: what waits is above it, what has happened below. Apricot, the color of the present in Gryning, with
 * its [label] ("Now") read as a heading by TalkBack.
 */
@Composable
fun ImornNowLine(label: String, modifier: Modifier = Modifier) {
    val color = ImornTheme.colors.secondary
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ImornTheme.spacing.sm),
    ) {
        Box(Modifier.weight(1f).height(2.dp).background(color))
        ImornText(
            text = label,
            style = ImornTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.semantics { heading() },
        )
        Box(Modifier.weight(1f).height(2.dp).background(color))
    }
}

/** A text field with a [label], in the app's colors and shape. */
@Composable
fun ImornTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = singleLine,
        shape = ImornTheme.shapes.small,
        modifier = modifier,
    )
}

/** Asks for one line of text, such as an own step. Confirming is only possible with something written. */
@Composable
fun ImornTextInputDialog(
    title: String,
    label: String,
    confirmText: String,
    dismissText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { ImornTextField(value = text, onValueChange = { text = it }, label = label, singleLine = true) },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    )
}

/** Asks before something that cannot be undone, such as deleting a chapter. */
@Composable
fun ImornConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    )
}
