package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 무지출 챌린지 판정 — 예산 기간과 독립, 일반 지출만 본다 (§12).
 */
class EvaluateNoSpendProgressUseCaseTest {

    private val useCase = EvaluateNoSpendProgressUseCase()

    private val start = LocalDate.of(2026, 7, 1)

    private fun settings(
        mode: NoSpendMode = NoSpendMode.FULL,
        capAmount: Long = 10_000L,
        targetDays: Int = 10,
        enabled: Boolean = true,
        startDate: LocalDate? = start
    ) = NoSpendChallengeSettings(enabled, mode, capAmount, targetDays, startDate)

    private fun expense(
        day: Int,
        amount: Long,
        type: ExpenseType = ExpenseType.GENERAL,
        isEssential: Boolean = false
    ) = Expense(
        id = day.toLong(),
        title = "지출",
        amount = amount,
        date = LocalDate.of(2026, 7, day),
        type = type,
        isEssential = isEssential
    )

    @Test
    fun `시작 전이면 진행 정보가 비어 있다`() {
        val progress = useCase(emptyList(), settings(startDate = null), LocalDate.of(2026, 7, 5))

        assertEquals(0, progress.successDays)
        assertEquals(0, progress.dayIndex)
        assertFalse(progress.isFinished)
    }

    @Test
    fun `지출이 없으면 지난 날 모두 성공 - 오늘은 확정하지 않는다`() {
        // 7월 5일 → 확정 대상은 1~4일 (4일)
        val progress = useCase(emptyList(), settings(), LocalDate.of(2026, 7, 5))

        assertEquals(4, progress.successDays)
        assertEquals(5, progress.dayIndex)
        assertTrue(progress.isTodayOnTrack)
    }

    @Test
    fun `완전 무지출 모드는 필수 표시라도 실패로 본다`() {
        val expenses = listOf(expense(day = 2, amount = 3_000L, isEssential = true))

        val progress = useCase(expenses, settings(NoSpendMode.FULL), LocalDate.of(2026, 7, 5))

        assertEquals(3, progress.successDays) // 1,3,4 성공 / 2일 실패
    }

    @Test
    fun `필수 허용 모드는 필수 지출을 무지출로 인정한다`() {
        val expenses = listOf(expense(day = 2, amount = 3_000L, isEssential = true))

        val progress = useCase(
            expenses,
            settings(NoSpendMode.ESSENTIAL_ALLOWED),
            LocalDate.of(2026, 7, 5)
        )

        assertEquals(4, progress.successDays)
    }

    @Test
    fun `필수 허용 모드에서 비필수 지출은 무지출을 깬다`() {
        val expenses = listOf(expense(day = 2, amount = 3_000L, isEssential = false))

        val progress = useCase(
            expenses,
            settings(NoSpendMode.ESSENTIAL_ALLOWED),
            LocalDate.of(2026, 7, 5)
        )

        assertEquals(3, progress.successDays)
    }

    @Test
    fun `상한 모드는 필수 제외 합계가 상한 이하면 성공`() {
        val expenses = listOf(
            expense(day = 2, amount = 8_000L),                       // 상한 이하 → 성공
            expense(day = 3, amount = 12_000L),                      // 상한 초과 → 실패
            expense(day = 4, amount = 50_000L, isEssential = true)    // 필수 제외 → 성공
        )

        val progress = useCase(
            expenses,
            settings(NoSpendMode.CAP, capAmount = 10_000L),
            LocalDate.of(2026, 7, 5)
        )

        assertEquals(3, progress.successDays) // 1,2,4
    }

    @Test
    fun `금고 준비금은 무지출을 깨지 않는다`() {
        val expenses = listOf(
            expense(day = 2, amount = 100_000L, type = ExpenseType.FUTURE_PREPARE)
        )

        val progress = useCase(expenses, settings(NoSpendMode.FULL), LocalDate.of(2026, 7, 5))

        assertEquals(4, progress.successDays)
    }

    @Test
    fun `연속 일수는 오늘부터 거꾸로 센다`() {
        // 2일에 지출 → 3,4일 성공, 오늘(5일) 진행 중 → 연속 3
        val expenses = listOf(expense(day = 2, amount = 5_000L))

        val progress = useCase(expenses, settings(), LocalDate.of(2026, 7, 5))

        assertEquals(3, progress.streak)
    }

    @Test
    fun `오늘 지출이 있으면 연속은 어제까지만 센다`() {
        val expenses = listOf(expense(day = 5, amount = 5_000L))

        val progress = useCase(expenses, settings(), LocalDate.of(2026, 7, 5))

        assertFalse(progress.isTodayOnTrack)
        assertEquals(4, progress.streak) // 1~4일
    }

    @Test
    fun `창이 끝나면 완료로 표시하고 전체 일수를 집계한다`() {
        // 10일 도전(7-1 ~ 7-10), 오늘 7-15
        val progress = useCase(emptyList(), settings(targetDays = 10), LocalDate.of(2026, 7, 15))

        assertTrue(progress.isFinished)
        assertEquals(10, progress.successDays)
        assertEquals(0, progress.dayIndex)
    }

    @Test
    fun `창 밖의 지출은 판정에 영향을 주지 않는다`() {
        // 도전 3일(7-1~7-3) 인데 7-8 에 지출
        val expenses = listOf(expense(day = 8, amount = 50_000L))

        val progress = useCase(expenses, settings(targetDays = 3), LocalDate.of(2026, 7, 10))

        assertEquals(3, progress.successDays)
    }
}
