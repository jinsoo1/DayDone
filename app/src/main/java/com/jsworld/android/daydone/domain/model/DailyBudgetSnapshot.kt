package com.jsworld.android.daydone.domain.model

/**
 * 오늘 기준 생활비 계산 결과 한 묶음.
 *
 * 오늘 탭·살까 말까·보류함·홈 위젯이 **모두 이 값을 쓴다**.
 * 한 곳에서만 계산하므로 화면끼리 숫자가 어긋날 수 없다.
 */
data class DailyBudgetSnapshot(
    val period: BudgetPeriod,
    val remainingDays: Int,

    /** 오늘 지출을 빼기 전 남은 생활비 — 오늘 권장 금액의 분자 */
    val budgetBeforeToday: Long,

    /** 오늘 지출까지 뺀 남은 생활비 (살까 말까·보류함이 쓰는 값) */
    val remainingPureBudget: Long,

    /** 오늘 권장 금액 */
    val todayRecommended: Long,

    /** 오늘 쓴 금액 */
    val todaySpent: Long,

    /** 오늘 남은 금액 (음수면 초과) */
    val todayLeft: Long,

    /**
     * 내일 권장 금액. 오늘이 기간 마지막 날이면 나눌 날이 없으므로 null.
     * (다음 기간 예산은 아직 모르니 0원을 지어내지 않는다)
     */
    val tomorrowRecommended: Long?
) {
    val isTodayOver: Boolean get() = todayLeft < 0L

    /** 초과 금액 (초과가 아니면 0) */
    val todayOverAmount: Long get() = if (isTodayOver) -todayLeft else 0L
}
