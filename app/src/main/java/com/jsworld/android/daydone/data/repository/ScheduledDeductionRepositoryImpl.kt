package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.ScheduledDeductionDao
import com.jsworld.android.daydone.data.local.entity.ScheduledDeductionEntity
import com.jsworld.android.daydone.data.mapper.toDomain
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class ScheduledDeductionRepositoryImpl @Inject constructor(
    private val scheduledDeductionDao: ScheduledDeductionDao
) : ScheduledDeductionRepository {

    override fun observeScheduledDeductions(): Flow<List<ScheduledDeduction>> {
        return scheduledDeductionDao.observeScheduledDeductions()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun addScheduledDeduction(
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth?,
        memo: String?
    ) {
        scheduledDeductionDao.insertScheduledDeduction(
            ScheduledDeductionEntity(
                title = title,
                amount = amount,
                type = type.name,
                withdrawalDay = withdrawalDay,
                startYearMonth = startYearMonth.toString(),
                endYearMonth = endYearMonth?.toString(),
                memo = memo
            )
        )
    }

    override suspend fun updateScheduledDeduction(
        id: Long,
        title: String,
        amount: Long,
        type: ScheduledDeductionType,
        withdrawalDay: Int
    ) {
        scheduledDeductionDao.updateScheduledDeduction(
            id = id,
            title = title,
            amount = amount,
            type = type.name,
            withdrawalDay = withdrawalDay
        )
    }

    override suspend fun updateEndYearMonth(
        id: Long,
        endYearMonth: YearMonth?
    ) {
        scheduledDeductionDao.updateEndYearMonth(
            id = id,
            endYearMonth = endYearMonth?.toString()
        )
    }

    override suspend fun deleteScheduledDeduction(id: Long) {
        scheduledDeductionDao.deleteScheduledDeduction(id)
    }
}