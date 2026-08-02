package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CalculateTodayDefenseLineUseCase {

    operator fun invoke(
        remainingPureBudget: Long,
        today: LocalDate,
        budgetPeriod: BudgetPeriod
    ): Long {
        val remainingDays = ChronoUnit.DAYS.between(
            today,
            budgetPeriod.endDate
        ).toInt() + 1

        if (remainingDays <= 0) return 0L

        return remainingPureBudget / remainingDays
    }
}