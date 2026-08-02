package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.YearMonth
import kotlin.collections.filter

class GetScheduledDeductionsInPeriodUseCase @Inject constructor() {

    operator fun invoke(
        deductions: List<ScheduledDeduction>,
        budgetPeriod: BudgetPeriod
    ): List<ScheduledDeduction> {
        return deductions.filter { deduction ->
            val withdrawalDate = resolveWithdrawalDateInPeriod(
                withdrawalDay = deduction.withdrawalDay,
                budgetPeriod = budgetPeriod
            ) ?: return@filter false

            val withdrawalYearMonth = YearMonth.from(withdrawalDate)

            val isAfterStartMonth =
                withdrawalYearMonth >= deduction.startYearMonth

            val isBeforeEndMonth =
                deduction.endYearMonth == null ||
                        withdrawalYearMonth <= deduction.endYearMonth

            isAfterStartMonth && isBeforeEndMonth
        }
    }

    private fun resolveWithdrawalDateInPeriod(
        withdrawalDay: Int,
        budgetPeriod: BudgetPeriod
    ): LocalDate? {
        var cursor = budgetPeriod.startDate

        while (!cursor.isAfter(budgetPeriod.endDate)) {
            val correctedDay = minOf(
                withdrawalDay,
                cursor.lengthOfMonth()
            )

            val candidate = cursor.withDayOfMonth(correctedDay)

            if (
                !candidate.isBefore(budgetPeriod.startDate) &&
                !candidate.isAfter(budgetPeriod.endDate)
            ) {
                return candidate
            }

            cursor = cursor.plusMonths(1).withDayOfMonth(1)
        }

        return null
    }
}