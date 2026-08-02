package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.NoSpendChallengeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoSpendChallengeRecordDao {

    /** 같은 시작일 기록은 다시 저장하지 않는다 (완료 감지가 여러 번 와도 안전). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: NoSpendChallengeRecordEntity)

    @Query(
        """
        SELECT * FROM no_spend_records
        ORDER BY startDate DESC
        """
    )
    fun observeRecords(): Flow<List<NoSpendChallengeRecordEntity>>
}
