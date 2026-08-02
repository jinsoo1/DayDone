package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingDoneUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isOnboardingDoneFlow
    }
}
