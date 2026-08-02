package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveScheduledDeductionsUseCase @Inject constructor(
    private val repository: ScheduledDeductionRepository
) {
    operator fun invoke(): Flow<List<ScheduledDeduction>> {
        return repository.observeScheduledDeductions()
    }
}