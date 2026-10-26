package se.imorn.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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
            containerColor = if (selected) colors.primaryContainer else ImornTheme.surfaces.card,
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

/**
 * How far along something is, such as a subflow's steps: a thin bar in the color of the present, on a quiet track.
 * [progress] is from 0 to 1. Decorative to TalkBack: say the numbers in text next to it.
 */
@Composable
fun ImornProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clearAndSetSemantics { },
        color = ImornTheme.colors.secondary,
        trackColor = ImornTheme.colors.surfaceVariant,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

/**
 * A small choice in a row, such as a template category: one of many, selected or not. The text is the caller's
 * string, so categories stay translated with the rest of the screen.
 */
@Composable
fun ImornFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ImornTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text) },
        modifier = modifier,
        shape = ImornTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ImornTheme.surfaces.card,
            labelColor = colors.onSurface,
            selectedContainerColor = colors.primaryContainer,
            selectedLabelColor = colors.onPrimaryContainer,
        ),
    )
}
