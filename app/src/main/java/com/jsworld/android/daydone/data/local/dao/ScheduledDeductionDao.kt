package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledDeductionDao {

    @Insert
    suspend fun insertScheduledDeduction(
        entity: ScheduledDeductionEntity
    )

    @Query(
        """
        SELECT * FROM scheduled_deductions
        ORDER BY createdAt DESC
        """
    )
    fun observeScheduledDeductions(): Flow<List<ScheduledDeductionEntity>>

    @Query(
        """
        UPDATE scheduled_deductions
        SET title = :title,
            amount = :amount,
            type = :type,
            withdrawalDay = :withdrawalDay
        WHERE id = :id
        """
    )
    suspend fun updateScheduledDeduction(
        id: Long,
        title: String,
        amount: Long,
        type: String,
        withdrawalDay: Int
    )

    @Query(
        """
        UPDATE scheduled_deductions
        SET endYearMonth = :endYearMonth
        WHERE id = :id
        """
    )
    suspend fun updateEndYearMonth(
        id: Long,
        endYearMonth: String?
    )

    @Query("DELETE FROM scheduled_deductions WHERE id = :id")
    suspend fun deleteScheduledDeduction(id: Long)
}