package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ScheduledDeductionAmountRepository
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject

class DeleteScheduledDeductionUseCase @Inject constructor(
    private val repository: ScheduledDeductionRepository,
    private val amountRepository: ScheduledDeductionAmountRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteScheduledDeduction(id)
        amountRepository.deleteForDeduction(id)
    }
}
