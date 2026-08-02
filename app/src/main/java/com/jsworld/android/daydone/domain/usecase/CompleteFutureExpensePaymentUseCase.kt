package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.model.FutureExpenseStatus
import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject
import java.time.LocalDate

/**
 * 납부 완료: 이미 준비된 만큼은 재차감하지 않고 **부족분만** 그 달 지출로 추가한다.
 * → 생활비에서 나간 총액 = 준비금 합 + 부족분 = totalAmount (초과/누락 0, 불변식).
 * 반복(매년)이면 목표월을 다음 해로 롤포워드하여 새 사이클 시작.
 */
class CompleteFutureExpensePaymentUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val futureExpenseRepository: FutureExpenseRepository
) {
    suspend operator fun invoke(
        status: FutureExpenseStatus,
        paymentDate: LocalDate
    ) {
        val item = status.item

        val shortfall = (item.totalAmount - status.preparedAmount).coerceAtLeast(0)
        if (shortfall > 0L) {
            // 부족분도 동일 경로(FUTURE_PREPARE, 같은 futureExpenseId)로 저장 → 총액 정확히 채움
            expenseRepository.addFuturePrepareExpense(
                futureExpenseId = item.id,
                title = "${item.title} 납부",
                amount = shortfall,
                date = paymentDate
            )
        }

        val nextTarget = if (item.repeat == FutureExpenseRepeat.YEARLY) {
            item.targetYearMonth.plusYears(1)
        } else {
            item.targetYearMonth
        }

        futureExpenseRepository.setPaymentState(
            id = item.id,
            lastPaidYearMonth = item.targetYearMonth,
            targetYearMonth = nextTarget
        )
    }
}
