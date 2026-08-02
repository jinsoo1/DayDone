package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import java.time.LocalDate

/**
 * 준비하기: 준비금을 그 달의 FUTURE_PREPARE 지출 1줄로 저장한다.
 * → 생활비 차감은 이 지출 한 번으로만 일어나고(유일 진실원),
 *   금고 "준비됨"은 이 지출들의 합으로 파생된다. 이중차감 없음.
 */
class PrepareFutureExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        item: FutureExpense,
        amount: Long,
        date: LocalDate
    ) {
        if (amount <= 0L) return

        expenseRepository.addFuturePrepareExpense(
            futureExpenseId = item.id,
            title = "${item.title} 준비",
            amount = amount,
            date = date
        )
    }
}
