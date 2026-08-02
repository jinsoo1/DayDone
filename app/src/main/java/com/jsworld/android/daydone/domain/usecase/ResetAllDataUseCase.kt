package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.DataResetRepository
import jakarta.inject.Inject

class ResetAllDataUseCase @Inject constructor(
    private val repository: DataResetRepository
) {
    suspend operator fun invoke() {
        repository.resetAll()
    }
}
