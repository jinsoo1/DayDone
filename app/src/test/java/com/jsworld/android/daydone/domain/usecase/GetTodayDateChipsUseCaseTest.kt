package com.jsworld.android.daydone.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 날짜 칩은 **예산 기간 경계를 넘어간다**.
 * 기간 첫날엔 지난 기간의 며칠이 칩으로 뜨므로, 내역을 읽어오는 쪽(TodayViewModel)이
 * 조회 범위를 기간으로 잘라버리면 그 칩들이 "내역이 없어요"로 보인다(v1.4.1 버그).
 * 조회 범위는 이 UseCase의 DAYS_BEFORE/DAYS_AFTER를 같이 써야 어긋나지 않는다.
 */
class GetTodayDateChipsUseCaseTest {

    private val chips = GetTodayDateChipsUseCase()
    private val period = GetCurrentBudgetPeriodUseCase()

    @Test
    fun `기간 첫날에는 칩이 지난 기간 날짜를 포함한다`() {
        val today = LocalDate.of(2026, 9, 1)
        val current = period(today, budgetStartDay = 1)

        val result = chips(
            today = today,
            selectedDate = today,
            expenseDates = emptySet(),
            scheduledDeductionDates = emptySet()
        )

        val beforePeriod = result.map { it.date }.filter { it < current.startDate }

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 29),
                LocalDate.of(2026, 8, 30),
                LocalDate.of(2026, 8, 31)
            ),
            beforePeriod
        )
    }

    @Test
    fun `기간 마지막 날에는 칩이 다음 기간 날짜를 포함한다`() {
        val today = LocalDate.of(2026, 9, 30)
        val current = period(today, budgetStartDay = 1)

        val result = chips(
            today = today,
            selectedDate = today,
            expenseDates = emptySet(),
            scheduledDeductionDates = emptySet()
        )

        val afterPeriod = result.map { it.date }.filter { it > current.endDate }

        assertEquals(
            listOf(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3)
            ),
            afterPeriod
        )
    }

    @Test
    fun `칩 범위는 오늘 앞뒤 상수와 일치한다`() {
        val today = LocalDate.of(2026, 9, 15)

        val result = chips(
            today = today,
            selectedDate = today,
            expenseDates = setOf(today.minusDays(2)),
            scheduledDeductionDates = setOf(today.plusDays(1))
        )

        assertEquals(
            today.minusDays(GetTodayDateChipsUseCase.DAYS_BEFORE.toLong()),
            result.first().date
        )
        assertEquals(
            today.plusDays(GetTodayDateChipsUseCase.DAYS_AFTER.toLong()),
            result.last().date
        )
        assertTrue(result.single { it.isToday }.date == today)
        assertTrue(result.single { it.hasExpense }.date == today.minusDays(2))
        assertTrue(result.single { it.hasScheduledDeduction }.date == today.plusDays(1))
    }
}
