package com.jsworld.android.daydone.data.repository

import com.jsworld.android.daydone.data.local.dao.FutureExpenseDao
import com.jsworld.android.daydone.data.local.entity.FutureExpenseEntity
import com.jsworld.android.daydone.data.mapper.toDomain
import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class FutureExpenseRepositoryImpl @Inject constructor(
    private val futureExpenseDao: FutureExpenseDao
) : FutureExpenseRepository {

    override fun observeAll(): Flow<List<FutureExpense>> {
        return futureExpenseDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun add(
        title: String,
        category: FutureExpenseCategory,
        totalAmount: Long,
        targetYearMonth: YearMonth,
        prepareStartYearMonth: YearMonth,
        repeat: FutureExpenseRepeat,
        memo: String?
    ): Long {
        return futureExpenseDao.insert(
            FutureExpenseEntity(
                title = title,
                category = category.name,
                totalAmount = totalAmount,
                targetYearMonth = targetYearMonth.toString(),
                prepareStartYearMonth = prepareStartYearMonth.toString(),
                repeatRule = repeat.name,
                memo = memo,
                lastPaidYearMonth = null
            )
        )
    }

    override suspend fun update(
        id: Long,
        title: String,
        category: FutureExpenseCategory,
        totalAmount: Long,
        targetYearMonth: YearMonth,
        prepareStartYearMonth: YearMonth,
        repeat: FutureExpenseRepeat,
        memo: String?
    ) {
        futureExpenseDao.update(
            id = id,
            title = title,
            category = category.name,
            totalAmount = totalAmount,
            targetYearMonth = targetYearMonth.toString(),
            prepareStartYearMonth = prepareStartYearMonth.toString(),
            repeatRule = repeat.name,
            memo = memo
        )
    }

    override suspend fun setPaymentState(
        id: Long,
        lastPaidYearMonth: YearMonth?,
        targetYearMonth: YearMonth
    ) {
        futureExpenseDao.updatePaymentState(
            id = id,
            lastPaidYearMonth = lastPaidYearMonth?.toString(),
            targetYearMonth = targetYearMonth.toString()
        )
    }

    override suspend fun delete(id: Long) {
        futureExpenseDao.delete(id)
    }
}
