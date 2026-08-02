package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import java.time.LocalDate

class GetCurrentBudgetPeriodUseCase {

    operator fun invoke(
        today: LocalDate,
        budgetStartDay: Int
    ): BudgetPeriod {
        val thisMonthStart = today.withDayOfMonth(
            minOf(budgetStartDay, today.lengthOfMonth())
        )

        val startDate = if (today >= thisMonthStart) {
            thisMonthStart
        } else {
            val previousMonth = today.minusMonths(1)
            previousMonth.withDayOfMonth(
                minOf(budgetStartDay, previousMonth.lengthOfMonth())
            )
        }

        val nextMonth = startDate.plusMonths(1)
        val nextStartDate = nextMonth.withDayOfMonth(
            minOf(budgetStartDay, nextMonth.lengthOfMonth())
        )

        val endDate = nextStartDate.minusDays(1)

        return BudgetPeriod(
            startDate = startDate,
            endDate = endDate
        )
    }
}