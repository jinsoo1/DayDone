package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.ExtraIncomeDao
import com.jsworld.android.daydone.data.local.entity.ExtraIncomeEntity
import com.jsworld.android.daydone.data.mapper.toDomain
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.repository.ExtraIncomeRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExtraIncomeRepositoryImpl @Inject constructor(
    private val extraIncomeDao: ExtraIncomeDao
) : ExtraIncomeRepository {

    override fun observeExtraIncomesByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<ExtraIncome>> {
        return extraIncomeDao.observeExtraIncomesByPeriod(
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addExtraIncome(
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String?
    ) {
        extraIncomeDao.insertExtraIncome(
            ExtraIncomeEntity(
                title = title,
                amount = amount,
                date = date.toString(),
                memo = memo
            )
        )
    }

    override suspend fun updateExtraIncome(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String?
    ) {
        extraIncomeDao.updateExtraIncome(
            id = id,
            title = title,
            amount = amount,
            date = date.toString(),
            memo = memo
        )
    }

    override suspend fun deleteExtraIncome(id: Long) {
        extraIncomeDao.deleteExtraIncome(id)
    }
}