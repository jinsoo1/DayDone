package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import java.time.YearMonth

/**
 * 특정 달(anchor month)의 예산 기간을 계산한다.
 * 기간 시작일은 [yearMonth] 안의 budgetStartDay 이며, 다음 기간 시작 하루 전까지가 끝이다.
 */
class GetBudgetPeriodForMonthUseCase {

    operator fun invoke(
        yearMonth: YearMonth,
        budgetStartDay: Int
    ): BudgetPeriod {
        val startDate = yearMonth.atDay(
            minOf(budgetStartDay, yearMonth.lengthOfMonth())
        )

        val nextMonth = yearMonth.plusMonths(1)
        val nextStartDate = nextMonth.atDay(
            minOf(budgetStartDay, nextMonth.lengthOfMonth())
        )

        return BudgetPeriod(
            startDate = startDate,
            endDate = nextStartDate.minusDays(1)
        )
    }
}
