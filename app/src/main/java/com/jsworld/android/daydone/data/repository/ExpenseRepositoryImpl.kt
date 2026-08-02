package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.ExpenseDao
import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import com.jsworld.android.daydone.data.mapper.toDomain
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun observeExpensesByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseDao.observeExpensesByPeriod(
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addExpense(
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean
    ) {
        expenseDao.insertExpense(
            ExpenseEntity(
                title = title,
                amount = amount,
                date = date.toString(),
                type = ExpenseType.GENERAL.name,
                isEssential = isEssential
            )
        )
    }

    override suspend fun updateExpense(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean
    ) {
        expenseDao.updateExpense(
            id = id,
            title = title,
            amount = amount,
            date = date.toString(),
            isEssential = isEssential
        )
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpense(expenseId)
    }

    override suspend fun getEarliestExpenseDate(): LocalDate? =
        expenseDao.getEarliestExpenseDate()
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    override suspend fun addFuturePrepareExpense(
        futureExpenseId: Long,
        title: String,
        amount: Long,
        date: LocalDate
    ) {
        expenseDao.insertExpense(
            ExpenseEntity(
                title = title,
                amount = amount,
                date = date.toString(),
                type = ExpenseType.FUTURE_PREPARE.name,
                futureExpenseId = futureExpenseId
            )
        )
    }

    override fun observeExpensesByFutureExpenseId(
        futureExpenseId: Long
    ): Flow<List<Expense>> {
        return expenseDao.observeExpensesByFutureExpenseId(futureExpenseId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeFuturePrepareExpenses(): Flow<List<Expense>> {
        return expenseDao.observeFuturePrepareExpenses()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun deleteExpensesByFutureExpenseId(futureExpenseId: Long) {
        expenseDao.deleteByFutureExpenseId(futureExpenseId)
    }
}