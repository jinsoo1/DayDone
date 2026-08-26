package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.domain.repository.NotificationSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(): Flow<NotificationSettings> = repository.observe()
}
