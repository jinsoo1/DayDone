package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import jakarta.inject.Inject
import java.time.LocalDate

class AddExtraIncomeUseCase @Inject constructor(
    private val repository: ExtraIncomeRepository
) {
    suspend operator fun invoke(
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String? = null
    ) {
        repository.addExtraIncome(
            title = title,
            amount = amount,
            date = date,
            memo = memo
        )
    }
}