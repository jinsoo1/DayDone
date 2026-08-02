package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.MonthlyBudgetDao
import com.jsworld.android.daydone.data.local.entity.MonthlyBudgetEntity
import com.jsworld.android.daydone.domain.repository.MonthlyBudgetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class MonthlyBudgetRepositoryImpl @Inject constructor(
    private val monthlyBudgetDao: MonthlyBudgetDao
) : MonthlyBudgetRepository {

    override fun observeEffectiveIncome(
        anchorMonth: YearMonth,
        default: Long
    ): Flow<Long> {
        return monthlyBudgetDao
            .observeEffectiveBudget(anchorMonth.toString())
            .map { entity -> entity?.income ?: default }
    }

    override suspend fun setIncome(
        anchorMonth: YearMonth,
        income: Long
    ) {
        monthlyBudgetDao.upsert(
            MonthlyBudgetEntity(
                anchorMonth = anchorMonth.toString(),
                income = income
            )
        )
    }
}
