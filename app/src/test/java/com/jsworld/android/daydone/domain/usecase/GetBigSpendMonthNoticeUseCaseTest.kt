package com.jsworld.android.daydone.domain.usecase

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/** 큰 지출 예상 달 안내 (docs/v1.4-design.md §1). */
class GetBigSpendMonthNoticeUseCaseTest {

    private val useCase = GetBigSpendMonthNoticeUseCase()

    @Test
    fun `설날이 든 달은 설날로 안내한다`() {
        val notice = useCase(YearMonth.of(2026, 2))

        assertTrue(notice!!.contains("설날"))
        assertTrue(notice.contains("금고"))
    }

    @Test
    fun `추석이 든 달은 추석으로 안내한다`() {
        assertTrue(useCase(YearMonth.of(2026, 9))!!.contains("추석"))
    }

    @Test
    fun `5월은 가정의 달로 안내한다`() {
        assertTrue(useCase(YearMonth.of(2026, 5))!!.contains("가정의 달"))
    }

    @Test
    fun `12월은 연말 모임으로 안내한다`() {
        assertTrue(useCase(YearMonth.of(2026, 12))!!.contains("연말"))
    }

    @Test
    fun `해당 없는 달은 안내하지 않는다`() {
        assertNull(useCase(YearMonth.of(2026, 3)))
        assertNull(useCase(YearMonth.of(2026, 7)))
    }

    @Test
    fun `음력 명절은 해마다 달이 다르다`() {
        // 2028년 설날은 1월, 추석은 10월
        assertTrue(useCase(YearMonth.of(2028, 1))!!.contains("설날"))
        assertTrue(useCase(YearMonth.of(2028, 10))!!.contains("추석"))
        assertNull(useCase(YearMonth.of(2028, 2)))
    }
}
