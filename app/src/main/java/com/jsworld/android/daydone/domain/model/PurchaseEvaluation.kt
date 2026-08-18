package com.jsworld.android.daydone.domain.model

/**
 * 살까 말까 결과 상태 (§13: 판정이 아니라 사실+뉘앙스).
 * 빠듯도 "살 수는 있어요"로 시작한다 — 앱이 말리는 게 아니라 트레이드오프를 보여줄 뿐.
 */
enum class PurchaseImpact {
    /** 거의 무변화 — 하루 권장 감소가 5% 미만 */
    NEGLIGIBLE,

    /** 여유 — 구매 후 하루 권장이 현재의 70% 이상 */
    COMFORTABLE,

    /** 빠듯 — 살 수는 있지만 남은 날들이 빠듯해짐 (0 ≤ 구매 후 권장 < 70%) */
    TIGHT,

    /** 불가 — 남은 생활비보다 가격이 큼 (금고 준비 제안) */
    IMPOSSIBLE
}

/**
 * 살까 말까 계산 결과.
 * currentDaily 는 오늘 탭과 같은 소스(남은 순수 생활비 ÷ 남은 일수)로 계산돼야 한다.
 */
data class PurchaseEvaluation(
    val currentDaily: Long,
    val afterDaily: Long,
    val impact: PurchaseImpact
)
