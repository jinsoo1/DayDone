package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRepository
import jakarta.inject.Inject

class UpdateNoSpendChallengeUseCase @Inject constructor(
    private val repository: NoSpendChallengeRepository
) {
    suspend operator fun invoke(settings: NoSpendChallengeSettings) {
        repository.update(settings)
    }
}
