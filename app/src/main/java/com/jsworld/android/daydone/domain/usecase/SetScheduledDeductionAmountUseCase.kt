package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ScheduledDeductionAmountRepository
import jakarta.inject.Inject
import java.time.YearMonth

class SetScheduledDeductionAmountUseCase @Inject constructor(
    private val repository: ScheduledDeductionAmountRepository
) {
    suspend operator fun invoke(
        deductionId: Long,
        anchorMonth: YearMonth,
        amount: Long
    ) {
        repository.setAmount(
            deductionId = deductionId,
            anchorMonth = anchorMonth,
            amount = amount
        )
    }
}
