package com.jsworld.android.daydone.domain.model

/**
 * 알림이 **무엇을 말할지**. 문장 조립은 하지 않는다.
 *
 * UseCase 엔 Context 가 없어 `getString` 을 쓸 수 없다. 그렇다고 `StringProvider` 를
 * 주입하면 domain 이 Android 에 의존하게 되고 순수 JVM 테스트가 깨진다(v1.5 설계 3-3).
 * 그래서 UseCase 는 종류와 인자만 정하고, 문장은 [com.jsworld.android.daydone.notification.NotificationTextRenderer]
 * 가 만든다.
 *
 * 저녁 제목 우선순위·합치기 규칙 같은 **판정**은 그대로 UseCase 에 남는다. 그게 이 앱의 자산이다.
 */
sealed interface NotificationText {

    // ── 아침 ──────────────────────────────────────────────
    /** "오늘은 {금액}까지 괜찮아요" */
    data class MorningTitle(val todayRecommended: Long) : NotificationText

    /** 권장 금액 안내가 꺼져 있을 때의 제목 */
    data object MorningTitleNoBudget : NotificationText

    data object MorningReportLine : NotificationText

    /** "이번 기간 {N}일 남았어요." */
    data class MorningRemainingDays(val remainingDays: Int) : NotificationText

    // ── 저녁: 기록 안내 / 무지출 축하 ───────────────────────
    data object NoSpendCelebrationTitle : NotificationText

    /** 연속 2일 이상일 때. 몇 일째인지 말해주는 게 이어가는 힘이 된다. */
    data class NoSpendCelebrationStreakBody(val streak: Int) : NotificationText

    /** 첫날. 아직 연속이 아니라 일수를 말할 게 없다. */
    data object NoSpendCelebrationFirstBody : NotificationText

    data object RecordNoticeTitle : NotificationText
    data object RecordNoticeBody : NotificationText

    // ── 저녁: 내일 나갈 돈 ─────────────────────────────────
    /** 한 건이면 항목명과 금액, 여러 건이면 건수와 합계. */
    data class UpcomingDeductionTitle(
        val count: Int,
        val firstTitle: String,
        val totalAmount: Long
    ) : NotificationText

    /**
     * 여러 건이면 항목명을 앞에 붙인다.
     *
     * ⚠️ 문구의 핵심은 "이미 생활비에선 빼둔 돈"이라는 재확인이다(§4·docs/v1.4-design.md).
     * 이 알림의 목적은 예산 경고가 아니라 출금 통장 잔액 확인 하나다.
     */
    data class UpcomingDeductionBody(val names: List<String>) : NotificationText

    // ── 보류함 30일 ───────────────────────────────────────
    data class HeldPurchaseTitle(val count: Int, val firstTitle: String) : NotificationText
    data class HeldPurchaseBody(val totalAmount: Long) : NotificationText

    // ── 조립 ──────────────────────────────────────────────
    /** 여러 안내를 한 건으로 합친 것. 표시 계층에서 공백으로 잇는다. */
    data class Joined(val parts: List<NotificationText>) : NotificationText

    /** 제목을 본문 문장으로 이어 쓸 때 문장부호를 맞춘다(문장부호 규칙은 로케일을 탄다). */
    data class AsSentence(val text: NotificationText) : NotificationText

    /**
     * 아직 타입으로 못 옮긴 완성 문자열.
     *
     * ⚠️ `GetBigSpendMonthNoticeUseCase` 전용 임시 통로다. 그쪽은 **로케일별 지식 주입**
     * (설·추석 음력 표)이라 v1.5 설계 3-6 에서 따로 다룬다. 다른 곳에서 쓰지 말 것.
     */
    data class Raw(val text: String) : NotificationText

    companion object {
        /** 이 일수부터 "N일째"라고 말한다. */
        const val STREAK_THRESHOLD = 2

        /** null 을 걸러 합친다. 남는 게 하나면 그대로, 없으면 null. */
        fun join(parts: List<NotificationText?>): NotificationText? {
            val kept = parts.filterNotNull()
            return when (kept.size) {
                0 -> null
                1 -> kept.first()
                else -> Joined(kept)
            }
        }
    }
}
