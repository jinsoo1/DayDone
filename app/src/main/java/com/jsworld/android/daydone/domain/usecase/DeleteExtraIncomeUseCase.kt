package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import jakarta.inject.Inject

class DeleteExtraIncomeUseCase @Inject constructor(
    private val repository: ExtraIncomeRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteExtraIncome(id)
    }
}