package com.jsworld.android.daydone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jsworld.android.daydone.data.local.entity.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MonthlyBudgetEntity)

    /**
     * 해당 달 이하(이전 포함)에서 가장 최근에 설정된 예산 레코드를 반환한다.
     * anchorMonth 가 "YYYY-MM" 문자열이므로 사전순 정렬 == 시간순 정렬.
     */
    @Query(
        """
        SELECT * FROM monthly_budgets
        WHERE anchorMonth <= :anchorMonth
        ORDER BY anchorMonth DESC
        LIMIT 1
        """
    )
    fun observeEffectiveBudget(anchorMonth: String): Flow<MonthlyBudgetEntity?>
}
