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

    // --- 말일 시작 (startDay 31, 달에 없는 날짜는 말일로 clamp — v1.1) ---

    @Test
    fun `시작일 31은 매월 말일 시작으로 동작한다`() {
        // 7/15는 6/30(6월 말일)에 시작한 기간 안
        val period = current(LocalDate.of(2026, 7, 15), budgetStartDay = 31)

        assertEquals(LocalDate.of(2026, 6, 30), period.startDate)
        assertEquals(LocalDate.of(2026, 7, 30), period.endDate)
    }

    @Test
    fun `시작일 31 - 오늘이 말일이면 새 기간 첫날`() {
        val period = current(LocalDate.of(2026, 7, 31), budgetStartDay = 31)

        assertEquals(LocalDate.of(2026, 7, 31), period.startDate)
        assertEquals(LocalDate.of(2026, 8, 30), period.endDate)
    }

    @Test
    fun `시작일 31 - 2월은 28일로 clamp 된다`() {
        val period = forMonth(YearMonth.of(2026, 2), budgetStartDay = 31)

        assertEquals(LocalDate.of(2026, 2, 28), period.startDate)
        assertEquals(LocalDate.of(2026, 3, 30), period.endDate)
    }

    @Test
    fun `시작일 30 - 2월에서도 월당 기간이 정확히 1개 (anchorMonth 불변식)`() {
        // 1~4월 각 기간의 시작일이 서로 다른 달에 하나씩 있는지
        val starts = (1..4).map { forMonth(YearMonth.of(2026, it), budgetStartDay = 30).startDate }

        assertEquals(
            listOf(
                LocalDate.of(2026, 1, 30),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 4, 30)
            ),
            starts
        )
        // 기간이 이어지는지 (빈 날/겹침 없음)
        (1..3).forEach { month ->
            val period = forMonth(YearMonth.of(2026, month), budgetStartDay = 30)
            val next = forMonth(YearMonth.of(2026, month + 1), budgetStartDay = 30)
            assertEquals(period.endDate.plusDays(1), next.startDate)
        }
    }

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
