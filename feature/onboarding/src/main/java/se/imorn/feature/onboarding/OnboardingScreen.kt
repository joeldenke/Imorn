package se.imorn.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import se.imorn.designsystem.component.ImornDawnScene
import se.imorn.designsystem.component.ImornGlassPanel
import se.imorn.designsystem.component.ImornPageIndicator
import se.imorn.designsystem.component.ImornSceneButton
import se.imorn.designsystem.component.ImornSceneText
import se.imorn.designsystem.component.ImornSceneTextButton
import se.imorn.designsystem.component.ImornStillGlass
import se.imorn.designsystem.theme.ImornTheme
import se.imorn.model.LifeStage

/** The welcome's pages, in order. */
enum class OnboardingPage { Welcome, Why, Stages }

/**
 * The welcome, stateless: three calm pages on the dawn scene. What Imorn is, why it helps (the reasons are the
 * product's promises, so they say only what the app really does), and "What fits you right now?" with several
 * choices instead of an age. *Skip* is always there, and nothing is asked that the app doesn't need.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onToggle: (LifeStage) -> Unit,
    onStart: () -> Unit,
    onSkip: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    initialPage: OnboardingPage = OnboardingPage.Welcome,
) {
    val pages = OnboardingPage.entries
    val pager = rememberPagerState(initialPage = initialPage.ordinal) { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex
    ImornDawnScene(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(contentPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ImornTheme.spacing.lg, end = ImornTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImornSceneText(
                    text = stringResource(R.string.onboarding_brand),
                    style = ImornTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                ImornSceneTextButton(text = stringResource(R.string.onboarding_skip), onClick = onSkip)
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                when (pages[page]) {
                    OnboardingPage.Welcome -> WelcomePage()
                    OnboardingPage.Why -> WhyPage()
                    OnboardingPage.Stages -> StagesPage(state, onToggle)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ImornTheme.spacing.lg, vertical = ImornTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ImornPageIndicator(
                    count = pages.size,
                    current = pager.currentPage,
                    description = stringResource(R.string.onboarding_page, pager.currentPage + 1, pages.size),
                    modifier = Modifier.weight(1f),
                )
                ImornSceneButton(
                    text = stringResource(if (last) R.string.onboarding_start else R.string.onboarding_next),
                    onClick = {
                        if (last) onStart() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    },
                )
            }
        }
    }
}

/** A page's frame: its content in a column with the scene's margins, scrolling when the text is large. */
@Composable
private fun Page(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ImornTheme.spacing.lg, vertical = ImornTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(ImornTheme.spacing.md),
    ) {
        content()
    }
}

@Composable
private fun Headline(@StringRes text: Int) {
    ImornSceneText(
        text = stringResource(text),
        style = ImornTheme.typography.displaySmall,
        modifier = Modifier.semantics { heading() },
    )
}

/** What Imorn is, in a headline and two sentences, high in the night sky above the sun. */
@Composable
private fun WelcomePage() {
    Page {
        Spacer(Modifier.height(ImornTheme.spacing.xl))
        Headline(R.string.onboarding_title)
        ImornSceneText(
            text = stringResource(R.string.onboarding_body),
            style = ImornTheme.typography.bodyLarge,
            muted = true,
        )
    }
}

/** Why Imorn helps: four promises, numbered, each one something the app really does. */
@Composable
private fun WhyPage() {
    Page {
        Headline(R.string.onboarding_why_title)
        Reasons.forEachIndexed { index, (title, body) ->
            ImornGlassPanel(modifier = Modifier.fillMaxWidth()) {
                ImornSceneText(
                    text = stringResource(R.string.onboarding_reason_number, index + 1),
                    style = ImornTheme.typography.labelLarge,
                    muted = true,
                )
                ImornSceneText(text = stringResource(title), style = ImornTheme.typography.titleMedium)
                ImornSceneText(text = stringResource(body), style = ImornTheme.typography.bodyMedium, muted = true)
            }
        }
    }
}

private val Reasons = listOf(
    R.string.onboarding_reason_steps to R.string.onboarding_reason_steps_body,
    R.string.onboarding_reason_three to R.string.onboarding_reason_three_body,
    R.string.onboarding_reason_dates to R.string.onboarding_reason_dates_body,
    R.string.onboarding_reason_private to R.string.onboarding_reason_private_body,
)

/** "What fits you right now?": several choices, as glass panels that light up in the sun's color. */
@Composable
private fun StagesPage(state: OnboardingUiState, onToggle: (LifeStage) -> Unit) {
    Page {
        Headline(R.string.onboarding_stages_title)
        ImornSceneText(
            text = stringResource(R.string.onboarding_stages_body),
            style = ImornTheme.typography.bodyMedium,
            muted = true,
        )
        LifeStage.entries.forEach { stage ->
            ImornGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                selected = stage in state.lifeStages,
                onClick = { onToggle(stage) },
            ) {
                ImornSceneText(text = stringResource(stage.title), style = ImornTheme.typography.titleSmall)
                ImornSceneText(
                    text = stringResource(stage.examples),
                    style = ImornTheme.typography.bodyMedium,
                    muted = true,
                )
            }
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

@Composable
private fun PreviewPage(page: OnboardingPage) {
    ImornTheme {
        ImornStillGlass {
            OnboardingScreen(
                state = OnboardingUiState(lifeStages = setOf(LifeStage.BuildingUp)),
                onToggle = {},
                onStart = {},
                onSkip = {},
                contentPadding = PaddingValues(),
                initialPage = page,
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 860)
@Composable
private fun OnboardingWelcomePreview() {
    PreviewPage(OnboardingPage.Welcome)
}

@Preview(widthDp = 400, heightDp = 860)
@Composable
private fun OnboardingWhyPreview() {
    PreviewPage(OnboardingPage.Why)
}

@Preview(widthDp = 400, heightDp = 860)
@Composable
private fun OnboardingStagesPreview() {
    PreviewPage(OnboardingPage.Stages)
}
