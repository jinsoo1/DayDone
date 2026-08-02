package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.FutureExpenseStatus
import jakarta.inject.Inject
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 이번 달(anchorMonth) 준비 제안액 = (남은 금액) ÷ (목표월까지 남은 개월, 이번 달 포함).
 * 준비 창(prepareStart ≤ 이번달 ≤ 목표월)이고 남은 금액이 있으며 미완료인 항목만.
 * 반환: itemId -> 이번 달 제안액
 */
class GetPrepareSuggestionsUseCase @Inject constructor() {

    operator fun invoke(
        statuses: List<FutureExpenseStatus>,
        anchorMonth: YearMonth
    ): Map<Long, Long> {
        return statuses.mapNotNull { status ->
            val item = status.item

            if (status.isCompleted) return@mapNotNull null
            if (status.remainingAmount <= 0L) return@mapNotNull null
            if (anchorMonth < item.prepareStartYearMonth) return@mapNotNull null
            if (anchorMonth > item.targetYearMonth) return@mapNotNull null

            val monthsLeft = ChronoUnit.MONTHS
                .between(anchorMonth, item.targetYearMonth)
                .toInt() + 1

            if (monthsLeft <= 0) return@mapNotNull null

            val suggested = (status.remainingAmount + monthsLeft - 1) / monthsLeft // ceil
            item.id to suggested
        }.toMap()
    }
}
