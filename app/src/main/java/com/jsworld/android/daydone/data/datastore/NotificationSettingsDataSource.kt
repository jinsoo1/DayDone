package com.jsworld.android.daydone.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.jsworld.android.daydone.domain.model.NotificationSettings
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationSettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val MORNING = booleanPreferencesKey("noti_morning_enabled")
        val MORNING_HOUR = intPreferencesKey("noti_morning_hour")
        val EVENING = booleanPreferencesKey("noti_evening_enabled")
        val EVENING_HOUR = intPreferencesKey("noti_evening_hour")
        val UPCOMING_DEDUCTION = booleanPreferencesKey("noti_upcoming_deduction_enabled")
        val HELD = booleanPreferencesKey("noti_held_enabled")
        val REPORT = booleanPreferencesKey("noti_report_enabled")
        val BIG_SPEND = booleanPreferencesKey("noti_big_spend_enabled")
    }

    val settingsFlow: Flow<NotificationSettings> = dataStore.data.map { p ->
        NotificationSettings(
            morningEnabled = p[Keys.MORNING] ?: false,
            morningHour = p[Keys.MORNING_HOUR] ?: NotificationSettings.DEFAULT_MORNING_HOUR,
            eveningEnabled = p[Keys.EVENING] ?: false,
            eveningHour = p[Keys.EVENING_HOUR] ?: NotificationSettings.DEFAULT_EVENING_HOUR,
            upcomingDeductionEnabled = p[Keys.UPCOMING_DEDUCTION] ?: false,
            heldPurchaseEnabled = p[Keys.HELD] ?: false,
            periodReportEnabled = p[Keys.REPORT] ?: false,
            bigSpendMonthEnabled = p[Keys.BIG_SPEND] ?: false
        )
    }

    suspend fun update(settings: NotificationSettings) {
        dataStore.edit { p ->
            p[Keys.MORNING] = settings.morningEnabled
            p[Keys.MORNING_HOUR] = settings.morningHour
            p[Keys.EVENING] = settings.eveningEnabled
            p[Keys.EVENING_HOUR] = settings.eveningHour
            p[Keys.UPCOMING_DEDUCTION] = settings.upcomingDeductionEnabled
            p[Keys.HELD] = settings.heldPurchaseEnabled
            p[Keys.REPORT] = settings.periodReportEnabled
            p[Keys.BIG_SPEND] = settings.bigSpendMonthEnabled
        }
    }
}
