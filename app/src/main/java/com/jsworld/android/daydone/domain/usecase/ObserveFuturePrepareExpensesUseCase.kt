package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/** 모든 준비금(FUTURE_PREPARE) 지출을 관찰한다. (항목별·기간별 합산용) */
class ObserveFuturePrepareExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> {
        return expenseRepository.observeFuturePrepareExpenses()
    }
}
