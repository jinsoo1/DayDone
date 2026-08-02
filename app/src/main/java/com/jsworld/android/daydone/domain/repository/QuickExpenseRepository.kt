package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.QuickExpense
import kotlinx.coroutines.flow.Flow

interface QuickExpenseRepository {

    fun observeQuickExpenses(): Flow<List<QuickExpense>>

    suspend fun addQuickExpense(
        title: String,
        amount: Long
    )

    suspend fun deleteQuickExpense(id: Long)
}