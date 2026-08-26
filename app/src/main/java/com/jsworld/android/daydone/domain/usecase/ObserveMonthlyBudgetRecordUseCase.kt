package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.MonthlyBudgetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * 해당 달에 적용되는 월별 예산 레코드(이월 포함). null 이면 레코드가 없어
 * `BudgetProfile` 기본 수입이 그대로 쓰이는 상태다.
 *
 * 설정 탭의 "월 수입(기본값)"이 이 달에 실제로 쓰이는지 알려주는 데 쓴다.
 */
class ObserveMonthlyBudgetRecordUseCase @Inject constructor(
    private val repository: MonthlyBudgetRepository
) {
    operator fun invoke(anchorMonth: YearMonth): Flow<Long?> =
        repository.observeIncomeRecord(anchorMonth)
}
