package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import kotlinx.coroutines.flow.Flow

interface NoSpendChallengeRecordRepository {

    fun observeRecords(): Flow<List<NoSpendChallengeRecord>>

    /** 같은 시작일 기록이 이미 있으면 무시된다. */
    suspend fun saveRecord(record: NoSpendChallengeRecord)
}
