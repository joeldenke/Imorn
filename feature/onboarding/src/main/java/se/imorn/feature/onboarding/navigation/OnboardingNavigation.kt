package se.imorn.feature.onboarding.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable
import se.imorn.feature.onboarding.OnboardingScreen
import se.imorn.feature.onboarding.OnboardingViewModel

/** The welcome at first start, shown on its own until it is done or skipped. */
@Serializable data object OnboardingRoute : NavKey

/**
 * The welcome's entry. Getting started or skipping marks it done in `SettingsStore`, and the app, which reads that,
 * switches to Imorgon by itself; so the entry needs no callback.
 */
fun EntryProviderScope<NavKey>.onboardingEntry(contentPadding: PaddingValues) {
    entry<OnboardingRoute> {
        val viewModel = metroViewModel<OnboardingViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        OnboardingScreen(
            state = state,
            onNameChange = viewModel::setName,
            onToggle = viewModel::toggle,
            onStart = viewModel::start,
            onSkip = viewModel::skip,
            contentPadding = contentPadding,
        )
    }
}
