package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.MonthlyBudgetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class ObserveEffectiveMonthlyBudgetUseCase @Inject constructor(
    private val repository: MonthlyBudgetRepository
) {
    operator fun invoke(
        anchorMonth: YearMonth,
        default: Long
    ): Flow<Long> {
        return repository.observeEffectiveIncome(
            anchorMonth = anchorMonth,
            default = default
        )
    }
}
