package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BigSpendNotice
import com.jsworld.android.daydone.domain.model.KoreanLunarHoliday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

/**
 * 큰 지출 예상 달 안내 (docs/v1.4-design.md §1).
 *
 * ⚠️ v1.5 국제화 3-6 에서 문구가 리소스로 나가면서 **문자열 비교 → 타입 비교**로 바꿨다.
 * 지켜야 할 건 문구가 아니라 **음력 표가 해마다 맞는지**다.
 */
class GetBigSpendMonthNoticeUseCaseTest {

    private val useCase = GetBigSpendMonthNoticeUseCase()

    @Test
    fun `설날이 든 달은 설날로 안내한다`() {
        assertEquals(
            BigSpendNotice.LunarHoliday(KoreanLunarHoliday.SEOLLAL),
            useCase(YearMonth.of(2026, 2))
        )
    }

    @Test
    fun `추석이 든 달은 추석으로 안내한다`() {
        assertEquals(
            BigSpendNotice.LunarHoliday(KoreanLunarHoliday.CHUSEOK),
            useCase(YearMonth.of(2026, 9))
        )
    }

    @Test
    fun `5월은 가정의 달로 안내한다`() {
        assertEquals(BigSpendNotice.FamilyMonth, useCase(YearMonth.of(2026, 5)))
    }

    @Test
    fun `12월은 연말 모임으로 안내한다`() {
        assertEquals(BigSpendNotice.YearEnd, useCase(YearMonth.of(2026, 12)))
    }

    @Test
    fun `해당 없는 달은 안내하지 않는다`() {
        assertNull(useCase(YearMonth.of(2026, 3)))
        assertNull(useCase(YearMonth.of(2026, 7)))
    }

    @Test
    fun `음력 명절은 해마다 달이 다르다`() {
        // 2028년 설날은 1월, 추석은 10월
        assertEquals(
            BigSpendNotice.LunarHoliday(KoreanLunarHoliday.SEOLLAL),
            useCase(YearMonth.of(2028, 1))
        )
        assertEquals(
            BigSpendNotice.LunarHoliday(KoreanLunarHoliday.CHUSEOK),
            useCase(YearMonth.of(2028, 10))
        )
        assertNull(useCase(YearMonth.of(2028, 2)))
    }
}
