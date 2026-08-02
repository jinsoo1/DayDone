package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject

class UpdateScheduledDeductionUseCase @Inject constructor(
    private val repository: ScheduledDeductionRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int
    ) {
        repository.updateScheduledDeduction(
            id = id,
            title = title,
            amount = amount,
            type = type,
            withdrawalDay = withdrawalDay
        )
    }
}
