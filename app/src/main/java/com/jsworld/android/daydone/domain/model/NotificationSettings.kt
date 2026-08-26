package com.jsworld.android.daydone.domain.model

/**
 * 알림 설정 (docs/v1.4-design.md).
 *
 * 기본값은 **전부 off** — 아무것도 켜지 않으면 권한을 요청하지 않는다.
 * "묻는 권한 0개"를 지키기 위한 것이고, 켤 때만 물어본다.
 */
data class NotificationSettings(
    /** 아침 권장 금액 */
    val morningEnabled: Boolean = false,
    /** 아침 알림 시각 (7·8·9시 중) */
    val morningHour: Int = DEFAULT_MORNING_HOUR,

    /** 저녁 기록 안내 — 오늘 지출이 0건일 때만 */
    val eveningEnabled: Boolean = false,
    /** 저녁 알림 시각 (20·21·22시 중) */
    val eveningHour: Int = DEFAULT_EVENING_HOUR,

    /** 내일 나가는 돈 — 저축·고정비 출금 전날 저녁 (통장 잔액 확인용) */
    val upcomingDeductionEnabled: Boolean = false,

    /** 보류함 30일 도래 */
    val heldPurchaseEnabled: Boolean = false,

    /** 새 기간 첫날 결산 리포트 */
    val periodReportEnabled: Boolean = false,

    /** 명절·주말이 많은 달 안내 */
    val bigSpendMonthEnabled: Boolean = false
) {
    /** 하나라도 켜져 있는가 — 채널 생성·예약 여부를 가른다. */
    val anyEnabled: Boolean
        get() = morningEnabled || eveningEnabled || upcomingDeductionEnabled ||
                heldPurchaseEnabled || periodReportEnabled || bigSpendMonthEnabled

    /** 아침에 함께 나가는 알림들(합쳐서 한 건으로 보낸다). */
    val anyMorningEnabled: Boolean
        get() = morningEnabled || heldPurchaseEnabled || periodReportEnabled ||
                bigSpendMonthEnabled

    /** 저녁에 함께 나가는 알림들(합쳐서 한 건으로 보낸다). */
    val anyEveningEnabled: Boolean
        get() = eveningEnabled || upcomingDeductionEnabled

    companion object {
        const val DEFAULT_MORNING_HOUR = 8
        const val DEFAULT_EVENING_HOUR = 21

        val MORNING_HOUR_CHOICES = listOf(7, 8, 9)
        val EVENING_HOUR_CHOICES = listOf(20, 21, 22)
    }
}
