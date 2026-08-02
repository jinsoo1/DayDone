package com.jsworld.android.daydone.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.jsworld.android.daydone.domain.model.BudgetProfile
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class BudgetProfileDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val MONTHLY_INCOME = longPreferencesKey("monthly_income")
        val PAYDAY = intPreferencesKey("payday")
        val BUDGET_START_DAY = intPreferencesKey("budget_start_day")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val FIRST_USE_EPOCH_DAY = longPreferencesKey("first_use_epoch_day")
        val PRE_JOIN_SPEND_HANDLED = booleanPreferencesKey("pre_join_spend_handled")
    }

    /** "가입 전 지출" 배너를 처리(입력 또는 건너뛰기)했는지. */
    val isPreJoinSpendHandledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[Keys.PRE_JOIN_SPEND_HANDLED] ?: false
        }

    suspend fun setPreJoinSpendHandled() {
        dataStore.edit { preferences ->
            preferences[Keys.PRE_JOIN_SPEND_HANDLED] = true
        }
    }

    val isOnboardingDoneFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[Keys.ONBOARDING_DONE] ?: false
        }

    suspend fun setOnboardingDone() {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_DONE] = true
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    val budgetProfileFlow: Flow<BudgetProfile> =
        dataStore.data.map { preferences ->
            BudgetProfile(
                monthlyIncome = preferences[Keys.MONTHLY_INCOME] ?: 3_000_000L,
                payday = preferences[Keys.PAYDAY] ?: 25,
                budgetStartDay = preferences[Keys.BUDGET_START_DAY] ?: 1,
                firstUseDate = preferences[Keys.FIRST_USE_EPOCH_DAY]
                    ?.let { LocalDate.ofEpochDay(it) }
            )
        }

    suspend fun updateFirstUseDate(date: LocalDate) {
        dataStore.edit { preferences ->
            preferences[Keys.FIRST_USE_EPOCH_DAY] = date.toEpochDay()
        }
    }

    /** 첫 사용일 없음(온보딩 기능 이전 설치) 상태로 되돌린다 — 백업 충실 복원용. */
    suspend fun clearFirstUseDate() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.FIRST_USE_EPOCH_DAY)
        }
    }

    suspend fun updateMonthlyIncome(monthlyIncome: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.MONTHLY_INCOME] = monthlyIncome
        }
    }

    suspend fun updatePayday(payday: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.PAYDAY] = payday
        }
    }

    suspend fun updateBudgetStartDay(budgetStartDay: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.BUDGET_START_DAY] = budgetStartDay
        }
    }
}