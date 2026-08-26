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

    /**
     * 해당 달에 적용되는 **월별 예산 레코드**만 관찰한다 (이월 포함, 폴백 없음).
     * null 이면 레코드가 없어 `BudgetProfile` 기본 수입이 그대로 쓰이는 상태.
     *
     * 설정 탭에서 "이 달은 월 탭에서 정한 예산이 우선"임을 알려주려면
     * 값이 같은지가 아니라 **레코드가 있는지**를 알아야 한다.
     */
    fun observeIncomeRecord(anchorMonth: YearMonth): Flow<Long?>

    suspend fun setIncome(
        anchorMonth: YearMonth,
        income: Long
    )
}
