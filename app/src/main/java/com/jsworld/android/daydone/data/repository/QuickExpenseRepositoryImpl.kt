package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.QuickExpenseDao
import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import com.jsworld.android.daydone.data.mapper.toDomain
import com.jsworld.android.daydone.domain.model.QuickExpense
import com.jsworld.android.daydone.domain.repository.QuickExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuickExpenseRepositoryImpl @Inject constructor(
    private val quickExpenseDao: QuickExpenseDao
) : QuickExpenseRepository {

    override fun observeQuickExpenses(): Flow<List<QuickExpense>> {
        return quickExpenseDao.observeQuickExpenses()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun addQuickExpense(
        title: String,
        amount: Long
    ) {
        val sortOrder = quickExpenseDao.getQuickExpenseCount()

        quickExpenseDao.insertQuickExpense(
            QuickExpenseEntity(
                title = title,
                amount = amount,
                sortOrder = sortOrder
            )
        )
    }

    override suspend fun deleteQuickExpense(id: Long) {
        quickExpenseDao.deleteQuickExpense(id)
    }
}