package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetProfile
import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveBudgetProfileUseCase @Inject constructor(
    private val budgetProfileRepository: BudgetProfileRepository
) {
    operator fun invoke(): Flow<BudgetProfile> {
        return budgetProfileRepository.budgetProfileFlow
    }
}