package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.PurchaseEvaluation
import com.jsworld.android.daydone.domain.model.PurchaseImpact
import jakarta.inject.Inject

/**
 * 살까 말까 — 지금 사면 남은 기간 하루 권장이 어떻게 달라지는지 계산한다.
 *
 * A = pureBudgetLeft ÷ remainingDays (오늘 탭의 "남은 순수 생활비 ÷ 남은 일수"와 동일)
 * B = (pureBudgetLeft − price) ÷ remainingDays
 *
 * 상태 판정 (순서 중요):
 * - 불가: 남은 생활비보다 가격이 크다 (R − P < 0)
 * - 거의 무변화: 권장 감소 < 5%
 * - 여유: B ≥ A의 70%
 * - 빠듯: 나머지 (0 ≤ B < A의 70%)
 */
class EvaluatePurchaseUseCase @Inject constructor() {

    operator fun invoke(
        pureBudgetLeft: Long,
        remainingDays: Int,
        price: Long
    ): PurchaseEvaluation {
        val days = remainingDays.coerceAtLeast(1)

        val afterBudget = pureBudgetLeft - price
        val currentDaily = pureBudgetLeft / days
        val afterDaily = afterBudget / days

        val impact = when {
            afterBudget < 0L -> PurchaseImpact.IMPOSSIBLE
            (currentDaily - afterDaily) * 20L < currentDaily -> PurchaseImpact.NEGLIGIBLE
            afterDaily * 10L >= currentDaily * 7L -> PurchaseImpact.COMFORTABLE
            else -> PurchaseImpact.TIGHT
        }

        return PurchaseEvaluation(
            currentDaily = currentDaily,
            afterDaily = afterDaily,
            impact = impact
        )
    }
}
