package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject

class UpdateBudgetProfileUseCase @Inject constructor(
    private val budgetProfileRepository: BudgetProfileRepository
) {

    suspend fun updateMonthlyIncome(monthlyIncome: Long) {
        budgetProfileRepository.updateMonthlyIncome(monthlyIncome)
    }

    suspend fun updateBudgetStartDay(budgetStartDay: Int) {
        budgetProfileRepository.updateBudgetStartDay(budgetStartDay)
    }

    suspend fun updatePayday(payday: Int) {
        budgetProfileRepository.updatePayday(payday)
    }
}