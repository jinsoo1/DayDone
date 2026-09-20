package com.jsworld.android.daydone.domain.model

/**
 * 큰 지출이 예상되는 달 안내 — **무엇을 알릴지**만 정한다.
 *
 * 파생이 아니라 **지식 주입**이라 로케일마다 달력 자체가 다르다(§15).
 * 한국은 설·추석이 음력이라 표가 필요하지만, 일본은 명절이 전부 양력 고정이라
 * 표 없이 월 규칙만으로 끝난다(1월 お正月 / 4월 新生活 / 5월 GW+自動車税 /
 * 8월 お盆 / 12월 年末年始).
 */
sealed interface BigSpendNotice {
    /** 음력 명절이 든 달. */
    data class LunarHoliday(val holiday: KoreanLunarHoliday) : BigSpendNotice

    /** 5월 — 가정의 달. */
    data object FamilyMonth : BigSpendNotice

    /** 12월 — 연말 모임. */
    data object YearEnd : BigSpendNotice

    /** 일본 — 전부 양력 고정이라 표 없이 월 규칙만으로 끝난다. */
    data class JapaneseSeason(val season: JapaneseBigSpendSeason) : BigSpendNotice
}

enum class KoreanLunarHoliday { SEOLLAL, CHUSEOK }

/** 일본에서 출비가 몰리는 달. 월이 고정이라 해마다 갱신할 표가 없다. */
enum class JapaneseBigSpendSeason(val month: Int) {
    NEW_YEAR(1),     // お正月 — 帰省・お年玉・初売り
    NEW_LIFE(4),     // 新生活 — 引っ越し・歓迎会
    GOLDEN_WEEK(5),  // GW + 自動車税 통지
    OBON(8),         // お盆 — 帰省
    YEAR_END(12)     // 年末年始 — 忘年会・帰省
}
