package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRecordRepository
import jakarta.inject.Inject

class SaveNoSpendChallengeRecordUseCase @Inject constructor(
    private val repository: NoSpendChallengeRecordRepository
) {
    suspend operator fun invoke(record: NoSpendChallengeRecord) {
        repository.saveRecord(record)
    }
}
