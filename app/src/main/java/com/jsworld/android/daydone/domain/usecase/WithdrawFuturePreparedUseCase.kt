package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/**
 * 준비금 일부 되돌리기: 현재 사이클에 준비해둔 금액에서 [amount] 만큼을 빼서 생활비로 되돌린다.
 * 음수 지출을 만들지 않고, 최근 준비금 지출부터 삭제/감액하여 정확히 [amount] 만큼 회수한다.
 */
class WithdrawFuturePreparedUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        futureExpenseId: Long,
        amount: Long,
        cycleAfter: YearMonth?
    ) {
        if (amount <= 0L) return

        var remaining = amount
        val prepares = expenseRepository
            .observeExpensesByFutureExpenseId(futureExpenseId)
            .first()
            .filter { it.amount > 0L && (cycleAfter == null || YearMonth.from(it.date) > cycleAfter) }
            .sortedByDescending { it.date }

        for (expense in prepares) {
            if (remaining <= 0L) break
            if (expense.amount <= remaining) {
                expenseRepository.deleteExpense(expense.id)
                remaining -= expense.amount
            } else {
                expenseRepository.updateExpense(
                    id = expense.id,
                    title = expense.title,
                    amount = expense.amount - remaining,
                    date = expense.date
                )
                remaining = 0L
            }
        }
    }
}
