package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import com.jsworld.android.daydone.domain.repository.FutureExpenseRepository
import jakarta.inject.Inject

/**
 * 준비 항목 삭제 시 **연결된 FUTURE_PREPARE 준비금 지출도 함께 롤백**한다.
 * (그래야 이미 차감됐던 준비금이 생활비로 되돌아와 재계산됨)
 */
class DeleteFutureExpenseUseCase @Inject constructor(
    private val futureExpenseRepository: FutureExpenseRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long) {
        expenseRepository.deleteExpensesByFutureExpenseId(id)
        futureExpenseRepository.delete(id)
    }
}
