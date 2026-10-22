package se.imorn.feature.onboarding

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import se.imorn.data.ProfileStore
import se.imorn.data.SettingsStore
import se.imorn.model.LifeStage
import se.imorn.model.Profile

/** What the welcome has so far: a name, if any, and the life stages that fit. */
data class OnboardingUiState(val name: String = "", val lifeStages: Set<LifeStage> = emptySet())

/**
 * The welcome at first start, light on purpose: a name and the life stages that fit, both optional. Getting started
 * saves them and marks the welcome done; skipping only marks it done. The stores save in their own scope, so the
 * saves finish even as this screen goes away.
 */
@ViewModelKey
@ContributesIntoMap(AppScope::class)
@Inject
class OnboardingViewModel(
    private val profiles: ProfileStore,
    private val settings: SettingsStore,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun setName(name: String) = _state.update { it.copy(name = name) }

    fun toggle(stage: LifeStage) = _state.update {
        it.copy(lifeStages = if (stage in it.lifeStages) it.lifeStages - stage else it.lifeStages + stage)
    }

    fun start() {
        val state = _state.value
        profiles.save(Profile(name = state.name, lifeStages = state.lifeStages))
        settings.setOnboarded(true)
    }

    fun skip() = settings.setOnboarded(true)
}
