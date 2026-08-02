package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveNoSpendChallengeUseCase @Inject constructor(
    private val repository: NoSpendChallengeRepository
) {
    operator fun invoke(): Flow<NoSpendChallengeSettings> {
        return repository.settingsFlow
    }
}
