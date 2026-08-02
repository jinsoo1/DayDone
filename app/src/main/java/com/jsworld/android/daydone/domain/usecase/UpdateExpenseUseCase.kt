package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import java.time.LocalDate

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean = false
    ) {
        expenseRepository.updateExpense(
            id = id,
            title = title,
            amount = amount,
            date = date,
            isEssential = isEssential
        )
    }
}
