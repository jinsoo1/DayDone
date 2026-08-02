package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.model.FutureExpenseStatus
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

/**
 * 준비 항목 + 연결된 FUTURE_PREPARE 지출을 합쳐 항목별 진행 상태를 파생한다.
 * 현재 사이클 준비됨 = lastPaidYearMonth 이후(초과) 월에 찍힌 준비금 합.
 */
class ObserveFutureExpenseStatusesUseCase @Inject constructor(
    private val futureExpenseRepository: FutureExpenseRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<FutureExpenseStatus>> {
        return combine(
            futureExpenseRepository.observeAll(),
            expenseRepository.observeFuturePrepareExpenses()
        ) { items, prepares ->
            items.map { item ->
                val currentPrepared = prepares
                    .filter { it.futureExpenseId == item.id && it.isInCurrentCycle(item) }
                    .sumOf { it.amount }

                val completed =
                    item.repeat == FutureExpenseRepeat.ONCE && item.lastPaidYearMonth != null

                val prepared = if (completed) item.totalAmount else currentPrepared
                val remaining = (item.totalAmount - prepared).coerceAtLeast(0)

                FutureExpenseStatus(
                    item = item,
                    preparedAmount = prepared,
                    remainingAmount = remaining,
                    isCompleted = completed
                )
            }
        }
    }

    private fun com.jsworld.android.daydone.domain.model.Expense.isInCurrentCycle(
        item: FutureExpense
    ): Boolean {
        val lastPaid = item.lastPaidYearMonth ?: return true
        return YearMonth.from(date) > lastPaid
    }
}
