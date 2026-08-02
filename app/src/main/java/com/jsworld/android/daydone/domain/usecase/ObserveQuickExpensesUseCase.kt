package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.QuickExpense
import com.jsworld.android.daydone.domain.repository.QuickExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveQuickExpensesUseCase @Inject constructor(
    private val repository: QuickExpenseRepository
) {
    operator fun invoke(): Flow<List<QuickExpense>> {
        return repository.observeQuickExpenses()
    }
}