package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.datastore.BudgetProfileDataSource
import com.jsworld.android.daydone.data.local.db.DayDoneDatabase
import com.jsworld.android.daydone.domain.repository.DataResetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataResetRepositoryImpl @Inject constructor(
    private val database: DayDoneDatabase,
    private val budgetProfileDataSource: BudgetProfileDataSource
) : DataResetRepository {

    override suspend fun resetAll() {
        withContext(Dispatchers.IO) {
            // 전 테이블 삭제 — 새 테이블이 추가돼도 자동 포함(§15 누락 방지)
            database.clearAllTables()
        }
        budgetProfileDataSource.clearAll()
    }
}
