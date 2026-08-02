package com.jsworld.android.daydone.data.mapper

import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import com.jsworld.android.daydone.domain.model.QuickExpense

fun QuickExpenseEntity.toDomain(): QuickExpense {
    return QuickExpense(
        id = id,
        title = title,
        amount = amount,
        sortOrder = sortOrder,
        isActive = isActive
    )
}