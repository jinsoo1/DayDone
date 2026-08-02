package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import jakarta.inject.Inject
import java.time.YearMonth

/**
 * 각 고정비/저축 항목의 anchorMonth 기준 유효 금액을 이월 규칙으로 계산해
 * amount 가 채워진 항목 리스트를 반환한다.
 * (anchorMonth 이하 오버라이드 중 가장 최근 값, 없으면 항목의 최초 금액)
 */
class ResolveScheduledDeductionAmountsUseCase @Inject constructor() {

    operator fun invoke(
        deductions: List<ScheduledDeduction>,
        overrides: List<ScheduledDeductionAmount>,
        anchorMonth: YearMonth
    ): List<ScheduledDeduction> {
        return deductions.map { deduction ->
            val resolved = overrides
                .filter {
                    it.deductionId == deduction.id && it.anchorMonth <= anchorMonth
                }
                .maxByOrNull { it.anchorMonth }
                ?.amount
                ?: deduction.amount

            deduction.copy(amount = resolved)
        }
    }
}
