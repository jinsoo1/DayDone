package com.jsworld.android.daydone.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * 예산 기간 계산 — 앱의 모든 집계가 이 경계에 의존한다(§3 불변식).
 */
class BudgetPeriodUseCaseTest {

    private val current = GetCurrentBudgetPeriodUseCase()
    private val forMonth = GetBudgetPeriodForMonthUseCase()

    @Test
    fun `시작일 1일이면 그 달 1일부터 말일까지`() {
        val period = current(LocalDate.of(2026, 7, 15), budgetStartDay = 1)

        assertEquals(LocalDate.of(2026, 7, 1), period.startDate)
        assertEquals(LocalDate.of(2026, 7, 31), period.endDate)
    }

    @Test
    fun `시작일 25일 - 오늘이 시작일 이후면 이번 달 기간`() {
        val period = current(LocalDate.of(2026, 7, 26), budgetStartDay = 25)

        assertEquals(LocalDate.of(2026, 7, 25), period.startDate)
        assertEquals(LocalDate.of(2026, 8, 24), period.endDate)
    }

    @Test
    fun `시작일 25일 - 오늘이 시작일 이전이면 지난 달 기간`() {
        val period = current(LocalDate.of(2026, 7, 23), budgetStartDay = 25)

        assertEquals(LocalDate.of(2026, 6, 25), period.startDate)
        assertEquals(LocalDate.of(2026, 7, 24), period.endDate)
    }

    @Test
    fun `오늘이 시작일 당일이면 이번 기간의 첫날`() {
        val period = current(LocalDate.of(2026, 7, 25), budgetStartDay = 25)

        assertEquals(LocalDate.of(2026, 7, 25), period.startDate)
    }

    @Test
    fun `연말 경계를 넘어간다`() {
        val period = current(LocalDate.of(2026, 12, 30), budgetStartDay = 25)

        assertEquals(LocalDate.of(2026, 12, 25), period.startDate)
        assertEquals(LocalDate.of(2027, 1, 24), period.endDate)
    }

    @Test
    fun `짧은 달은 시작일을 말일로 clamp 한다`() {
        // 2026-02 는 28일까지 → startDay 31 은 2월 28일로 clamp
        val period = forMonth(YearMonth.of(2026, 2), budgetStartDay = 31)

        assertEquals(LocalDate.of(2026, 2, 28), period.startDate)
        assertEquals(LocalDate.of(2026, 3, 30), period.endDate)
    }

    @Test
    fun `같은 달에 대해 두 UseCase 결과가 일치한다`() {
        val today = LocalDate.of(2026, 7, 15)
        val fromToday = current(today, budgetStartDay = 10)
        val fromMonth = forMonth(YearMonth.of(2026, 7), budgetStartDay = 10)

        assertEquals(fromMonth, fromToday)
    }

    @Test
    fun `월당 기간은 하나이며 앞 기간 끝과 다음 기간 시작이 맞물린다`() {
        val july = forMonth(YearMonth.of(2026, 7), budgetStartDay = 10)
        val august = forMonth(YearMonth.of(2026, 8), budgetStartDay = 10)

        assertEquals(august.startDate, july.endDate.plusDays(1))
    }
}
