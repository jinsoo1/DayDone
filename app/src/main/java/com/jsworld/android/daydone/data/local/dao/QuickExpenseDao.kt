package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.QuickExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickExpenseDao {

    @Query(
        """
        SELECT * FROM quick_expenses
        WHERE isActive = 1
        ORDER BY sortOrder ASC, createdAt ASC
        """
    )
    fun observeQuickExpenses(): Flow<List<QuickExpenseEntity>>

    @Insert
    suspend fun insertQuickExpense(entity: QuickExpenseEntity)

    @Query("DELETE FROM quick_expenses WHERE id = :id")
    suspend fun deleteQuickExpense(id: Long)

    @Query("SELECT COUNT(*) FROM quick_expenses")
    suspend fun getQuickExpenseCount(): Int
}