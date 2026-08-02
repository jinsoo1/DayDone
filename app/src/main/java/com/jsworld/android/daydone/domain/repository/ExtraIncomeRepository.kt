package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.ExtraIncome
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExtraIncomeRepository {

    fun observeExtraIncomesByPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<ExtraIncome>>

    suspend fun addExtraIncome(
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String?
    )

    suspend fun updateExtraIncome(
        id: Long,
        title: String,
        amount: Long,
        date: LocalDate,
        memo: String?
    )

    suspend fun deleteExtraIncome(id: Long)
}