package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import java.time.LocalDate

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean = false
    ) {
        expenseRepository.addExpense(
            title = title,
            amount = amount,
            date = date,
            isEssential = isEssential
        )
    }
}