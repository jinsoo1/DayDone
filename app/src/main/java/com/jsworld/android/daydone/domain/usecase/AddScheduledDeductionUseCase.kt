package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject
import java.time.YearMonth

class AddScheduledDeductionUseCase @Inject constructor(
    private val repository: ScheduledDeductionRepository
) {
    suspend operator fun invoke(
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth? = null,
        memo: String? = null
    ) {
        repository.addScheduledDeduction(
            title = title,
            amount = amount,
            type = type,
            withdrawalDay = withdrawalDay,
            startYearMonth = startYearMonth,
            endYearMonth = endYearMonth,
            memo = memo
        )
    }
}