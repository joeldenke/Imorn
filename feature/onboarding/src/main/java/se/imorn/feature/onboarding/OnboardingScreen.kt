package se.imorn.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.imorn.designsystem.component.ImornBodyText
import se.imorn.designsystem.component.ImornButton
import se.imorn.designsystem.component.ImornChoiceCard
import se.imorn.designsystem.component.ImornScaffold
import se.imorn.designsystem.component.ImornText
import se.imorn.designsystem.component.ImornTextButton
import se.imorn.designsystem.component.ImornTextField
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.LifeStage
import se.imorn.ui.PreviewData

/**
 * The welcome, stateless: what Imorn is in two sentences, a name (optional) and "What fits you right now?" with
 * several choices instead of an age. Everything can be skipped.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNameChange: (String) -> Unit,
    onToggle: (LifeStage) -> Unit,
    onStart: () -> Unit,
    onSkip: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    ImornScaffold(modifier = modifier) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(scaffoldPadding)
                .padding(contentPadding)
                .padding(horizontal = ImornTheme.spacing.md, vertical = ImornTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
        ) {
            ImornText(
                text = stringResource(R.string.onboarding_title),
                style = ImornTheme.typography.headlineMedium,
                color = ImornTheme.colors.onBackground,
            )
            ImornBodyText(text = stringResource(R.string.onboarding_body))
            ImornTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.onboarding_name),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            ImornText(
                text = stringResource(R.string.onboarding_stages_title),
                style = ImornTheme.typography.titleMedium,
                color = ImornTheme.colors.onBackground,
            )
            ImornBodyText(text = stringResource(R.string.onboarding_stages_body))
            LifeStage.entries.forEach { stage ->
                ImornChoiceCard(
                    selected = stage in state.lifeStages,
                    onClick = { onToggle(stage) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ImornText(text = stringResource(stage.title), style = ImornTheme.typography.titleSmall)
                    ImornText(
                        text = stringResource(stage.examples),
                        style = ImornTheme.typography.bodyMedium,
                        color = ImornTheme.colors.onSurfaceVariant,
                    )
                }
            }
            ImornButton(
                text = stringResource(R.string.onboarding_start),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
            ImornTextButton(text = stringResource(R.string.onboarding_skip), onClick = onSkip)
            ImornText(
                text = stringResource(R.string.onboarding_privacy),
                style = ImornTheme.typography.bodySmall,
                color = ImornTheme.colors.onSurfaceVariant,
            )
        }
    }
}

private val LifeStage.title
    get() = when (this) {
        LifeStage.OnMyOwn -> R.string.stage_on_my_own
        LifeStage.BuildingUp -> R.string.stage_building_up
        LifeStage.Midlife -> R.string.stage_midlife
        LifeStage.NextChapter -> R.string.stage_next_chapter
        LifeStage.NewInSweden -> R.string.stage_new_in_sweden
        LifeStage.SomethingHard -> R.string.stage_something_hard
    }

private val LifeStage.examples
    get() = when (this) {
        LifeStage.OnMyOwn -> R.string.stage_on_my_own_examples
        LifeStage.BuildingUp -> R.string.stage_building_up_examples
        LifeStage.Midlife -> R.string.stage_midlife_examples
        LifeStage.NextChapter -> R.string.stage_next_chapter_examples
        LifeStage.NewInSweden -> R.string.stage_new_in_sweden_examples
        LifeStage.SomethingHard -> R.string.stage_something_hard_examples
    }

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun OnboardingPreview() {
    ImornTheme {
        OnboardingScreen(
            state = OnboardingUiState(name = PreviewData.NAME, lifeStages = setOf(LifeStage.BuildingUp)),
            onNameChange = {},
            onToggle = {},
            onStart = {},
            onSkip = {},
            contentPadding = PaddingValues(),
        )
    }
}
