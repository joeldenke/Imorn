package se.imorn.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import se.imorn.designsystem.theme.ImornTheme
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

/** A secondary action beside the screen's [ImornButton]: outlined, so the main action is the one that stands out. */
@Composable
fun ImornSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text = text)
    }
}

/** The quietest action: text only, for "Back", "Not now" and the like. */
@Composable
fun ImornTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text = text)
    }
}

/**
 * A card to choose, such as a template or an answer: big enough for the thumb, and marked with the primary color
 * when [selected]. Reads as a radio button to TalkBack.
 */
@Composable
fun ImornChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ImornTheme.colors
    Card(
        onClick = onClick,
        modifier = modifier.semantics {
            this.selected = selected
            role = Role.RadioButton
        },
        shape = ImornTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryContainer else colors.surface,
            contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline),
    ) {
        Column(
            modifier = Modifier.padding(ImornTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.xs),
        ) {
            content()
        }
    }
}

/**
 * Picks a day in a dialog. Days are local dates: the picker works in UTC midnights, which this converts both ways,
 * so the day picked is the day saved whatever the time zone. The texts come from the caller's strings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImornDatePickerDialog(
    initial: LocalDate?,
    confirmText: String,
    dismissText: String,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial?.let { it.toEpochDay() * MILLIS_PER_DAY })
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onPick(LocalDate.ofEpochDay(Math.floorDiv(it, MILLIS_PER_DAY))) }
                },
                enabled = state.selectedDateMillis != null,
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
    ) {
        DatePicker(state = state)
    }
}
