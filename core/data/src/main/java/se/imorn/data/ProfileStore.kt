package se.imorn.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import se.imorn.model.LifeStage
import se.imorn.model.Profile

/** The user's life stages in Preferences DataStore (file `profile`), on the device only. */
@SingleIn(AppScope::class)
class ProfileStore internal constructor(context: Context, scope: CoroutineScope, cipher: DatastoreCipher) {
    @Inject constructor(context: Context, cipher: DatastoreCipher) : this(context, storeScope(), cipher)

    private val store = PreferencesStore(context, NAME, scope, cipher)

    /** The saved profile, kept in memory. Null until the first read is done. */
    val profile: StateFlow<Profile?> = store.snapshots {
        Profile(lifeStages = it[LIFE_STAGES].orEmpty().mapNotNull(LifeStage::fromId).toSet())
    }

    /** Saves the profile. A name an earlier version asked for is removed on the way: the app no longer keeps one. */
    fun save(profile: Profile) = store.edit {
        it.remove(LEGACY_NAME)
        it[LIFE_STAGES] = profile.lifeStages.map { stage -> stage.id }.toSet()
    }

    private companion object {
        const val NAME = "profile"
        val LEGACY_NAME = stringPreferencesKey("name")
        val LIFE_STAGES = stringSetPreferencesKey("life_stages")
    }
}
