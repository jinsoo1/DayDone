package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionAmountRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveScheduledDeductionAmountsUseCase @Inject constructor(
    private val repository: ScheduledDeductionAmountRepository
) {
    operator fun invoke(): Flow<List<ScheduledDeductionAmount>> {
        return repository.observeAll()
    }
}
