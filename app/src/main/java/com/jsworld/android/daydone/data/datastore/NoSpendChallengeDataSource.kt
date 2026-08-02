package com.jsworld.android.daydone.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class NoSpendChallengeDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("nospend_enabled")
        val MODE = stringPreferencesKey("nospend_mode")
        val CAP_AMOUNT = longPreferencesKey("nospend_cap_amount")
        val TARGET_DAYS = intPreferencesKey("nospend_target_days")
        val START_DATE = longPreferencesKey("nospend_start_epoch_day")
    }

    val settingsFlow: Flow<NoSpendChallengeSettings> =
        dataStore.data.map { preferences ->
            NoSpendChallengeSettings(
                enabled = preferences[Keys.ENABLED] ?: false,
                mode = preferences[Keys.MODE]
                    ?.let { runCatching { NoSpendMode.valueOf(it) }.getOrNull() }
                    ?: NoSpendMode.ESSENTIAL_ALLOWED,
                capAmount = preferences[Keys.CAP_AMOUNT] ?: 10_000L,
                targetDays = preferences[Keys.TARGET_DAYS] ?: 10,
                startDate = preferences[Keys.START_DATE]?.let { LocalDate.ofEpochDay(it) }
            )
        }

    suspend fun update(settings: NoSpendChallengeSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.ENABLED] = settings.enabled
            preferences[Keys.MODE] = settings.mode.name
            preferences[Keys.CAP_AMOUNT] = settings.capAmount
            preferences[Keys.TARGET_DAYS] = settings.targetDays
            val start = settings.startDate
            if (start != null) {
                preferences[Keys.START_DATE] = start.toEpochDay()
            } else {
                preferences.remove(Keys.START_DATE)
            }
        }
    }
}
