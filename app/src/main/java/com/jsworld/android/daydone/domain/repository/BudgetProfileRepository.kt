package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.BudgetProfile
import kotlinx.coroutines.flow.Flow

interface BudgetProfileRepository {

    val budgetProfileFlow: Flow<BudgetProfile>

    val isOnboardingDoneFlow: Flow<Boolean>

    suspend fun updateMonthlyIncome(monthlyIncome: Long)

    suspend fun updatePayday(payday: Int)

    suspend fun updateBudgetStartDay(budgetStartDay: Int)

    suspend fun setOnboardingDone()

    suspend fun updateFirstUseDate(date: java.time.LocalDate)

    val isPreJoinSpendHandledFlow: Flow<Boolean>

    suspend fun setPreJoinSpendHandled()

    suspend fun clearAll()
}