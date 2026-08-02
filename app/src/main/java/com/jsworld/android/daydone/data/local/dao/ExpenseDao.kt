package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query(
        """
        SELECT * FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeExpensesByPeriod(
        startDate: String,
        endDate: String
    ): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT * FROM expenses
        WHERE date = :date
        ORDER BY createdAt DESC
        """
    )
    fun observeExpensesByDate(
        date: String
    ): Flow<List<ExpenseEntity>>

    @Query(
        """
        UPDATE expenses
        SET title = :title,
            amount = :amount,
            date = :date,
            isEssential = :isEssential
        WHERE id = :id
        """
    )
    suspend fun updateExpense(
        id: Long,
        title: String,
        amount: Long,
        date: String,
        isEssential: Boolean
    )

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    /** 전체 기록 중 가장 오래된 지출 날짜 — "기록을 시작한 날" 판정용. */
    @Query("SELECT MIN(date) FROM expenses")
    suspend fun getEarliestExpenseDate(): String?

    @Query(
        """
        SELECT * FROM expenses
        WHERE futureExpenseId = :futureExpenseId
        ORDER BY date ASC, createdAt ASC
        """
    )
    fun observeExpensesByFutureExpenseId(
        futureExpenseId: Long
    ): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses WHERE futureExpenseId = :futureExpenseId")
    suspend fun deleteByFutureExpenseId(futureExpenseId: Long)

    @Query(
        """
        SELECT * FROM expenses
        WHERE type = 'FUTURE_PREPARE'
        ORDER BY date ASC, createdAt ASC
        """
    )
    fun observeFuturePrepareExpenses(): Flow<List<ExpenseEntity>>
}