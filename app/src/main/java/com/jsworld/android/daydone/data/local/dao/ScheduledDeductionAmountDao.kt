package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionAmountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledDeductionAmountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScheduledDeductionAmountEntity)

    @Query("SELECT * FROM scheduled_deduction_amounts")
    fun observeAll(): Flow<List<ScheduledDeductionAmountEntity>>

    @Query("DELETE FROM scheduled_deduction_amounts WHERE deductionId = :deductionId")
    suspend fun deleteByDeductionId(deductionId: Long)
}
