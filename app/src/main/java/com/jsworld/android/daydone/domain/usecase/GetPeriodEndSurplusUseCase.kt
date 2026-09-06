package com.jsworld.android.daydone.domain.usecase

import jakarta.inject.Inject

/**
 * 기간 막바지에 "지금 페이스면 마지막 날 남는 돈"이 충분히 크면 그 금액을 돌려준다.
 * 오늘 탭이 이 값을 받아 "미리 금고에 옮겨둘까요" 한 줄과 버튼을 띄운다.
 *
 * 순수 함수. 예상 잔액 식은 **리포트(`BuildMonthlyReportUseCase.projectedLeftover`)와 같다**
 * — `남은 순수 생활비 − 하루 평균 일반 지출 × 남은 일수`. 두 화면 숫자가 다르면 신뢰가 깨진다.
 *
 * 왜 이게 필요한가: 마지막 며칠에 권장 금액이 15만원으로 뜨면 앱이 "오늘 15만원 써도 괜찮아요"
 * 라고 말하는 셈인데, 그 돈은 아껴서 지켜낸 돈이지 쓸 허가가 아니다. 앱의 약속(미래에 나갈 돈을
 * 미리 빼둔다)대로 금고로 옮길 길을 그 순간에 보여준다.
 *
 * 안내가 잔소리가 되지 않도록 조건은 좁다: 남은 일수가 [LATE_DAYS] 이하이고,
 * 예상 잔액이 기간 총 순수 생활비의 [MIN_SURPLUS_PERCENT]% 이상일 때만.
 * 금고에 옮기거나 남은 날 더 쓰면 잔액이 줄어 조건이 저절로 풀린다 — 별도 "닫기" 상태가 없다.
 */
class GetPeriodEndSurplusUseCase @Inject constructor() {

    companion object {
        /** 기간 마지막 며칠부터 안내할지 */
        const val LATE_DAYS = 5

        /** 총 순수 생활비 대비 이만큼 이상 남을 때만 안내 */
        const val MIN_SURPLUS_PERCENT = 10
    }

    /**
     * @param remaining 이 기간 순수 생활비에서 **기록된 지출 전부**를 뺀 값
     *   (리포트의 `remaining` 과 같다 — 미래 날짜에 미리 적어둔 지출도 이미 나갈 돈이라 뺀다)
     * @param generalSpent 이 기간 일반 지출 합 (준비금 제외 — 리포트 dailyAverage 와 같은 기준)
     * @param totalPureBudget 이 기간 총 순수 생활비 = 예산 + 추가수익 − 예정 차감
     * @param dayIndex 오늘이 기간 며칠째 (1-based)
     * @param remainingDays 오늘 포함 남은 일수
     * @return 안내할 예상 잔액. 조건에 안 맞으면 null.
     */
    operator fun invoke(
        remaining: Long,
        generalSpent: Long,
        totalPureBudget: Long,
        dayIndex: Int,
        remainingDays: Int
    ): Long? {
        if (totalPureBudget <= 0L || dayIndex < 1 || remainingDays < 1) return null
        if (remainingDays > LATE_DAYS) return null

        // 이번 기간에 적은 지출이 하나도 없으면 "다 남았다"가 되어버린다.
        // 안 쓴 게 아니라 안 적은 것일 수 있으니 그 유저에겐 권하지 않는다.
        if (generalSpent <= 0L) return null

        val dailyAverage = generalSpent / dayIndex
        val projectedLeftover = remaining - dailyAverage * remainingDays

        val threshold = totalPureBudget * MIN_SURPLUS_PERCENT / 100
        return if (projectedLeftover >= threshold) projectedLeftover else null
    }
}
