package se.imorn.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.coach.AssistantAvailability
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornCard
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.feature.today.R
import se.imorn.ui.PreviewData

/**
 * Today, stateless: it renders the state and reports intents, so every branch is previewable. The assistant card
 * drives the on-device model; everything else is where Imorn's project checklist will go.
 */
@Composable
fun TodayScreen(
    state: TodayUiState,
    onAskAssistant: () -> Unit,
    onDownloadAssistant: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    ImornScaffold(title = stringResource(R.string.today_screen_title), modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornCard(modifier = Modifier.fillMaxWidth()) {
                ImornText(
                    text = stringResource(R.string.today_next_step_label),
                    style = ImornTheme.typography.labelLarge,
                    color = ImornTheme.colors.onSurfaceVariant,
                )
                ImornBodyText(text = stringResource(R.string.today_next_step))
            }
            ImornCard(modifier = Modifier.fillMaxWidth()) {
                ImornText(
                    text = stringResource(R.string.assistant_title),
                    style = ImornTheme.typography.titleMedium,
                    color = ImornTheme.colors.onSurface,
                )
                ImornBodyText(text = availabilityText(state.availability))
                state.message?.let { ImornBodyText(text = it) }
                if (state.writing) {
                    ImornBodyText(text = stringResource(R.string.assistant_writing))
                }
                if (state.failed) {
                    ImornText(
                        text = stringResource(R.string.assistant_failed),
                        style = ImornTheme.typography.bodyMedium,
                        color = ImornTheme.colors.error,
                    )
                }
                when (state.availability) {
                    AssistantAvailability.Downloadable, AssistantAvailability.Unavailable ->
                        ImornButton(
                            text = stringResource(R.string.assistant_download),
                            onClick = onDownloadAssistant,
                        )

                    AssistantAvailability.Available ->
                        ImornButton(
                            text = stringResource(R.string.assistant_ask),
                            onClick = onAskAssistant,
                            enabled = !state.writing,
                        )

                    AssistantAvailability.Downloading, AssistantAvailability.Unknown -> Unit
                }
            }
        }
    }
}

@Composable
private fun availabilityText(availability: AssistantAvailability): String = when (availability) {
    AssistantAvailability.Unknown -> stringResource(R.string.assistant_checking)
    AssistantAvailability.Unavailable -> stringResource(R.string.assistant_unavailable)
    AssistantAvailability.Downloadable -> stringResource(R.string.assistant_downloadable)
    AssistantAvailability.Downloading -> stringResource(R.string.assistant_downloading)
    AssistantAvailability.Available -> stringResource(R.string.assistant_available)
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    ImornTheme {
        TodayScreen(
            state = TodayUiState(
                availability = AssistantAvailability.Available,
                message = PreviewData.ASSISTANT_MESSAGE,
            ),
            onAskAssistant = {},
            onDownloadAssistant = {},
            contentPadding = PaddingValues(),
        )
    }
}
