package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExpenseRepository {

    fun observeExpensesByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>>

    suspend fun addExpense(
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean = false
    )

    suspend fun updateExpense(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        isEssential: Boolean = false
    )

    suspend fun deleteExpense(expenseId: Long)

    /** 전체 기록 중 가장 오래된 지출 날짜 (기록 시작일). 기록이 없으면 null. */
    suspend fun getEarliestExpenseDate(): LocalDate?

    // --- 준비금(미래 지출) 연동 ---

    suspend fun addFuturePrepareExpense(
        futureExpenseId: Long,
        title: String,
        amount: Long,
        date: LocalDate
    )

    fun observeExpensesByFutureExpenseId(
        futureExpenseId: Long
    ): Flow<List<Expense>>

    fun observeFuturePrepareExpenses(): Flow<List<Expense>>

    suspend fun deleteExpensesByFutureExpenseId(futureExpenseId: Long)
}