package com.jsworld.android.daydone.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface MonthlyBudgetRepository {

    /**
     * 해당 달의 유효 예산을 관찰한다.
     * 명시적으로 저장된 값이 없으면 직전 달의 값을 이월하고,
     * 그마저 없으면 [default] 를 사용한다.
     */
    fun observeEffectiveIncome(
        anchorMonth: YearMonth,
        default: Long
    ): Flow<Long>

    suspend fun setIncome(
        anchorMonth: YearMonth,
        income: Long
    )
}
