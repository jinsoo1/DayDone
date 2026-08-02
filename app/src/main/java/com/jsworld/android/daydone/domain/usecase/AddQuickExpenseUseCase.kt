package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.QuickExpenseRepository
import jakarta.inject.Inject

class AddQuickExpenseUseCase @Inject constructor(
    private val repository: QuickExpenseRepository
) {
    suspend operator fun invoke(
        title: String,
        amount: Long
    ) {
        repository.addQuickExpense(
            title = title,
            amount = amount
        )
    }
}