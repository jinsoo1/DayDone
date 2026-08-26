package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.datastore.NotificationSettingsDataSource
import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.domain.repository.NotificationSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class NotificationSettingsRepositoryImpl @Inject constructor(
    private val dataSource: NotificationSettingsDataSource
) : NotificationSettingsRepository {

    override fun observe(): Flow<NotificationSettings> = dataSource.settingsFlow

    override suspend fun update(settings: NotificationSettings) = dataSource.update(settings)
}
