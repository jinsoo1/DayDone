package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpense
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject

/**
 * 납부 완료 취소: 가장 최근 납부를 되돌린다.
 * - 반복(매년): 목표월과 마지막 납부월을 각각 1년 전으로 되돌려 직전 사이클로 복귀.
 * - 1회: 납부 기록을 지워 다시 "준비 중" 상태로.
 * (납부 때 추가된 부족분 지출은 이미 쓴 돈이라 유지 → 준비금으로 남음. 필요하면 '준비금 빼기'로 회수)
 */
class UndoFutureExpensePaymentUseCase @Inject constructor(
    private val repository: FutureExpenseRepository
) {
    suspend operator fun invoke(item: FutureExpense) {
        val lastPaid = item.lastPaidYearMonth ?: return

        if (item.repeat == FutureExpenseRepeat.YEARLY) {
            repository.setPaymentState(
                id = item.id,
                lastPaidYearMonth = lastPaid.minusYears(1),
                targetYearMonth = item.targetYearMonth.minusYears(1)
            )
        } else {
            repository.setPaymentState(
                id = item.id,
                lastPaidYearMonth = null,
                targetYearMonth = item.targetYearMonth
            )
        }
    }
}
