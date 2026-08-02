package com.jsworld.android.daydone.domain.repository

import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface FutureExpenseRepository {

    fun observeAll(): Flow<List<FutureExpense>>

    suspend fun add(
        title: String,
        category: FutureExpenseCategory,
        totalAmount: Long,
        targetYearMonth: YearMonth,
        prepareStartYearMonth: YearMonth,
        repeat: FutureExpenseRepeat,
        memo: String?
    ): Long

    suspend fun update(
        id: Long,
        title: String,
        category: FutureExpenseCategory,
        totalAmount: Long,
        targetYearMonth: YearMonth,
        prepareStartYearMonth: YearMonth,
        repeat: FutureExpenseRepeat,
        memo: String?
    )

    /** 납부 완료 처리(사이클 종료). 반복 항목이면 다음 목표월로 롤포워드. */
    suspend fun setPaymentState(
        id: Long,
        lastPaidYearMonth: YearMonth?,
        targetYearMonth: YearMonth
    )

    suspend fun delete(id: Long)
}
