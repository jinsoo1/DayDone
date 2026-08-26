package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun observe(): Flow<NotificationSettings>
    suspend fun update(settings: NotificationSettings)
}
