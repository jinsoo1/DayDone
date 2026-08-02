package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ObserveExpensesByPeriodUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseRepository.observeExpensesByPeriod(
            startDate = startDate,
            endDate = endDate
        )
    }
}