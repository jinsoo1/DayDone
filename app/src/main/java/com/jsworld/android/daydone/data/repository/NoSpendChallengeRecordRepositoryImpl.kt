package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.NoSpendChallengeRecordDao
import com.jsworld.android.daydone.data.local.entity.NoSpendChallengeRecordEntity
import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.repository.NoSpendChallengeRecordRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class NoSpendChallengeRecordRepositoryImpl @Inject constructor(
    private val dao: NoSpendChallengeRecordDao
) : NoSpendChallengeRecordRepository {

    override fun observeRecords(): Flow<List<NoSpendChallengeRecord>> {
        return dao.observeRecords().map { entities ->
            entities.map { entity ->
                NoSpendChallengeRecord(
                    startDate = LocalDate.parse(entity.startDate),
                    targetDays = entity.targetDays,
                    successDays = entity.successDays,
                    mode = runCatching { NoSpendMode.valueOf(entity.mode) }
                        .getOrDefault(NoSpendMode.ESSENTIAL_ALLOWED),
                    capAmount = entity.capAmount
                )
            }
        }
    }

    override suspend fun saveRecord(record: NoSpendChallengeRecord) {
        dao.insertRecord(
            NoSpendChallengeRecordEntity(
                startDate = record.startDate.toString(),
                targetDays = record.targetDays,
                successDays = record.successDays,
                mode = record.mode.name,
                capAmount = record.capAmount
            )
        )
    }
}
