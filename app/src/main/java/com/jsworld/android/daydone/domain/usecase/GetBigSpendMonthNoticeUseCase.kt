package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BigSpendNotice
import com.jsworld.android.daydone.domain.model.KoreanLunarHoliday
import jakarta.inject.Inject
import java.time.YearMonth

/**
 * 어느 달에 큰 지출이 몰리는지 아는 달력. 로케일마다 다르다.
 *
 * 1.6.0 에서 일본 달력을 여기에 꽂는다(§15). 지금은 한국 하나뿐이라 선택 로직을
 * 만들지 않는다 — 고를 게 없는데 고르는 코드를 먼저 만들면 검증할 수가 없다.
 */
interface BigSpendCalendar {
    fun noticeFor(month: YearMonth): BigSpendNotice?
}

/**
 * 한국 달력. 설·추석은 음력이라 java.time 으로 계산할 수 없어 **표로 둔다**(연 1회 갱신).
 */
object KoreanBigSpendCalendar : BigSpendCalendar {

    override fun noticeFor(month: YearMonth): BigSpendNotice? {
        val key = "%04d-%02d".format(month.year, month.monthValue)

        LUNAR_HOLIDAYS[key]?.let { return BigSpendNotice.LunarHoliday(it) }

        return when (month.monthValue) {
            5 -> BigSpendNotice.FamilyMonth
            12 -> BigSpendNotice.YearEnd
            else -> null
        }
    }

    /** 설·추석이 든 달 (음력 기준이라 직접 관리) */
    private val LUNAR_HOLIDAYS = mapOf(
        "2026-02" to KoreanLunarHoliday.SEOLLAL,
        "2026-09" to KoreanLunarHoliday.CHUSEOK,
        "2027-02" to KoreanLunarHoliday.SEOLLAL,
        "2027-09" to KoreanLunarHoliday.CHUSEOK,
        "2028-01" to KoreanLunarHoliday.SEOLLAL,
        "2028-10" to KoreanLunarHoliday.CHUSEOK,
        "2029-02" to KoreanLunarHoliday.SEOLLAL,
        "2029-09" to KoreanLunarHoliday.CHUSEOK,
        "2030-02" to KoreanLunarHoliday.SEOLLAL,
        "2030-09" to KoreanLunarHoliday.CHUSEOK
    )
}

/** 큰 지출이 예상되는 달 안내. 해당 없으면 null. */
class GetBigSpendMonthNoticeUseCase @Inject constructor() {
    // 1.6.0 에서 로케일로 고른다. 지금은 Hilt 바인딩을 만들 이유가 없다 — 구현이 하나뿐이다.
    private val calendar: BigSpendCalendar = KoreanBigSpendCalendar

    operator fun invoke(month: YearMonth): BigSpendNotice? = calendar.noticeFor(month)
}
