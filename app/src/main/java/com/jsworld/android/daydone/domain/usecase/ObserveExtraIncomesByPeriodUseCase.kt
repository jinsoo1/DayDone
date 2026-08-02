package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ObserveExtraIncomesByPeriodUseCase @Inject constructor(
    private val repository: ExtraIncomeRepository
) {
    operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<ExtraIncome>> {
        return repository.observeExtraIncomesByPeriod(
            startDate = startDate,
            endDate = endDate
        )
    }
}