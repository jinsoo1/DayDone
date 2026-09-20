package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BigSpendNotice
import com.jsworld.android.daydone.domain.model.JapaneseBigSpendSeason
import com.jsworld.android.daydone.domain.model.KoreanLunarHoliday
import com.jsworld.android.daydone.presentation.util.AppLocale
import jakarta.inject.Inject
import java.time.YearMonth
import java.util.Locale

/**
 * 어느 달에 큰 지출이 몰리는지 아는 달력. 로케일마다 다르다.
 *
 * 1.6.0 에서 일본 달력을 여기에 꽂는다(§15). 지금은 한국 하나뿐이라 선택 로직을
 * 만들지 않는다 — 고를 게 없는데 고르는 코드를 먼저 만들면 검증할 수가 없다.
 */
interface BigSpendCalendar {
    fun noticeFor(month: YearMonth): BigSpendNotice?

    companion object {
        /** 문구가 있는 언어에만 그 나라 달력을 준다. 그 외는 한국(AppLocale 폴백과 같은 규칙). */
        fun forLocale(locale: Locale): BigSpendCalendar =
            if (locale.language == "ja") JapaneseBigSpendCalendar else KoreanBigSpendCalendar
    }
}

/**
 * 일본 달력. 명절이 **전부 양력 고정**이라 표가 없다 — 연 1회 갱신 부담이 사라진다.
 * 달마다 말하지 않는다(§13 "매달 말할 거리를 찾는 순간 잔소리 앱") — 5개 달만.
 */
object JapaneseBigSpendCalendar : BigSpendCalendar {
    override fun noticeFor(month: YearMonth): BigSpendNotice? =
        JapaneseBigSpendSeason.entries
            .firstOrNull { it.month == month.monthValue }
            ?.let { BigSpendNotice.JapaneseSeason(it) }
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
    // 호출 때마다 고른다 — 앱을 켜둔 채 단말 언어를 바꿔도 따라오게.
    private val calendar: BigSpendCalendar
        get() = BigSpendCalendar.forLocale(AppLocale.current)

    operator fun invoke(month: YearMonth): BigSpendNotice? = calendar.noticeFor(month)
}
