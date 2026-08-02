package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ScheduledDeductionAmountRepository {

    fun observeAll(): Flow<List<ScheduledDeductionAmount>>

    suspend fun setAmount(
        deductionId: Long,
        anchorMonth: YearMonth,
        amount: Long
    )

    suspend fun deleteForDeduction(deductionId: Long)
}
