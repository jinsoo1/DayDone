package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.datastore.BudgetProfileDataSource
import com.jsworld.android.daydone.domain.model.BudgetProfile
import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class BudgetProfileRepositoryImpl @Inject constructor(
    private val budgetProfileDataSource: BudgetProfileDataSource
) : BudgetProfileRepository {

    override val budgetProfileFlow: Flow<BudgetProfile>
        get() = budgetProfileDataSource.budgetProfileFlow

    override val isOnboardingDoneFlow: Flow<Boolean>
        get() = budgetProfileDataSource.isOnboardingDoneFlow

    override suspend fun updateMonthlyIncome(monthlyIncome: Long) {
        budgetProfileDataSource.updateMonthlyIncome(monthlyIncome)
    }

    override suspend fun setOnboardingDone() {
        budgetProfileDataSource.setOnboardingDone()
    }

    override suspend fun updateFirstUseDate(date: java.time.LocalDate) {
        budgetProfileDataSource.updateFirstUseDate(date)
    }

    override val isPreJoinSpendHandledFlow: Flow<Boolean>
        get() = budgetProfileDataSource.isPreJoinSpendHandledFlow

    override suspend fun setPreJoinSpendHandled() {
        budgetProfileDataSource.setPreJoinSpendHandled()
    }

    override suspend fun clearAll() {
        budgetProfileDataSource.clearAll()
    }

    override suspend fun updatePayday(payday: Int) {
        budgetProfileDataSource.updatePayday(payday)
    }

    override suspend fun updateBudgetStartDay(budgetStartDay: Int) {
        budgetProfileDataSource.updateBudgetStartDay(budgetStartDay)
    }
}