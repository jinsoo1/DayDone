package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.QuickExpenseRepository
import jakarta.inject.Inject

class DeleteQuickExpenseUseCase @Inject constructor(
    private val repository: QuickExpenseRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteQuickExpense(id)
    }
}