package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ExtraIncomeDao {

    @Insert
    suspend fun insertExtraIncome(entity: ExtraIncomeEntity)

    @Query(
        """
        SELECT * FROM extra_incomes
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeExtraIncomesByPeriod(
        startDate: String,
        endDate: String
    ): Flow<List<ExtraIncomeEntity>>

    @Query("DELETE FROM extra_incomes WHERE id = :id")
    suspend fun deleteExtraIncome(id: Long)

    @Query(
        """
        UPDATE extra_incomes
        SET title = :title,
            amount = :amount,
            date = :date,
            memo = :memo
        WHERE id = :id
        """
    )
    suspend fun updateExtraIncome(
        id: Long,
        title: String,
        amount: Long,
        date: String,
        memo: String?
    )
}