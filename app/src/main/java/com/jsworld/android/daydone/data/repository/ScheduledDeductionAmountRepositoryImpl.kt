package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionAmountDao
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionAmountEntity
import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionAmountRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class ScheduledDeductionAmountRepositoryImpl @Inject constructor(
    private val dao: ScheduledDeductionAmountDao
) : ScheduledDeductionAmountRepository {

    override fun observeAll(): Flow<List<ScheduledDeductionAmount>> {
        return dao.observeAll().map { entities ->
            entities.map {
                ScheduledDeductionAmount(
                    deductionId = it.deductionId,
                    anchorMonth = YearMonth.parse(it.anchorMonth),
                    amount = it.amount
                )
            }
        }
    }

    override suspend fun setAmount(
        deductionId: Long,
        anchorMonth: YearMonth,
        amount: Long
    ) {
        dao.upsert(
            ScheduledDeductionAmountEntity(
                deductionId = deductionId,
                anchorMonth = anchorMonth.toString(),
                amount = amount
            )
        )
    }

    override suspend fun deleteForDeduction(deductionId: Long) {
        dao.deleteByDeductionId(deductionId)
    }
}
