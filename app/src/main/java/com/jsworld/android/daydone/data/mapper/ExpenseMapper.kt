package com.jsworld.android.daydone.data.mapper

import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import java.time.LocalDate

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        title = title,
        amount = amount,
        date = LocalDate.parse(date),
        type = ExpenseType.valueOf(type),
        futureExpenseId = futureExpenseId,
        isEssential = isEssential
    )
}