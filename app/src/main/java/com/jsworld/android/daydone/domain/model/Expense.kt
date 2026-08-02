package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

data class Expense(
    val id: Long,
    val title: String,
    val amount: Long,
    val date: LocalDate,
    val type: ExpenseType,
    val futureExpenseId: Long? = null,
    val isEssential: Boolean = false
)