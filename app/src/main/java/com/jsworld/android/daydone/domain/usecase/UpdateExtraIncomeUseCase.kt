package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import jakarta.inject.Inject
import java.time.LocalDate

class UpdateExtraIncomeUseCase @Inject constructor(
    private val repository: ExtraIncomeRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String? = null
    ) {
        repository.updateExtraIncome(
            id = id,
            title = title,
            amount = amount,
            date = date,
            memo = memo
        )
    }
}