package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FutureExpenseDao {

    @Insert
    suspend fun insert(entity: FutureExpenseEntity): Long

    @Query(
        """
        SELECT * FROM future_expenses
        ORDER BY targetYearMonth ASC, createdAt ASC
        """
    )
    fun observeAll(): Flow<List<FutureExpenseEntity>>

    @Query(
        """
        UPDATE future_expenses
        SET title = :title,
            category = :category,
            totalAmount = :totalAmount,
            targetYearMonth = :targetYearMonth,
            prepareStartYearMonth = :prepareStartYearMonth,
            repeatRule = :repeatRule,
            memo = :memo
        WHERE id = :id
        """
    )
    suspend fun update(
        id: Long,
        title: String,
        category: String,
        totalAmount: Long,
        targetYearMonth: String,
        prepareStartYearMonth: String,
        repeatRule: String,
        memo: String?
    )

    @Query(
        """
        UPDATE future_expenses
        SET lastPaidYearMonth = :lastPaidYearMonth,
            targetYearMonth = :targetYearMonth
        WHERE id = :id
        """
    )
    suspend fun updatePaymentState(
        id: Long,
        lastPaidYearMonth: String?,
        targetYearMonth: String
    )

    @Query("DELETE FROM future_expenses WHERE id = :id")
    suspend fun delete(id: Long)
}
