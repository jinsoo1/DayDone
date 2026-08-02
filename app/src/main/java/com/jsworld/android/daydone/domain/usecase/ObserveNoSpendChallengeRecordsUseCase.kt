package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRecordRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveNoSpendChallengeRecordsUseCase @Inject constructor(
    private val repository: NoSpendChallengeRecordRepository
) {
    operator fun invoke(): Flow<List<NoSpendChallengeRecord>> {
        return repository.observeRecords()
    }
}
