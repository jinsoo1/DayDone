package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject

/** 온보딩 완료: 월 수입·예산 시작일·시작 사용일을 저장하고 완료 플래그를 세운다. */
class CompleteOnboardingUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    suspend operator fun invoke(
        monthlyIncome: Long,
        budgetStartDay: Int
    ) {
        repository.updateMonthlyIncome(monthlyIncome)
        repository.updateBudgetStartDay(budgetStartDay)
        repository.updateFirstUseDate(java.time.LocalDate.now())
        repository.setOnboardingDone()
    }
}
