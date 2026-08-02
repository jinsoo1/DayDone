package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ScheduledDeductionRepository {

    fun observeScheduledDeductions(): Flow<List<ScheduledDeduction>>

    suspend fun addScheduledDeduction(
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth?,
        memo: String?
    )

    suspend fun updateScheduledDeduction(
        id: Long,
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int
    )

    /** 종료월 설정: 그 달까지만 반영되고 이후 기간에서 제외된다. null이면 무기한. */
    suspend fun updateEndYearMonth(
        id: Long,
        endYearMonth: YearMonth?
    )

    suspend fun deleteScheduledDeduction(id: Long)
}