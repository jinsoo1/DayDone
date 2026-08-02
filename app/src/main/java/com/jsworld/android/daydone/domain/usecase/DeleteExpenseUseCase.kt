package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long) {
        expenseRepository.deleteExpense(id)
    }
}
