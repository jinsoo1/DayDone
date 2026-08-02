package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.MonthlyBudgetRepository
import jakarta.inject.Inject
import java.time.YearMonth

class SetMonthlyBudgetUseCase @Inject constructor(
    private val repository: MonthlyBudgetRepository
) {
    suspend operator fun invoke(
        anchorMonth: YearMonth,
        income: Long
    ) {
        repository.setIncome(
            anchorMonth = anchorMonth,
            income = income
        )
    }
}
