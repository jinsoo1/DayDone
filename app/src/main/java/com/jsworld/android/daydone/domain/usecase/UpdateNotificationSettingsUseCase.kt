package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.domain.repository.NotificationSettingsRepository
import jakarta.inject.Inject

class UpdateNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    suspend operator fun invoke(settings: NotificationSettings) = repository.update(settings)
}
