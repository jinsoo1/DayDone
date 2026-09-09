package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * 권장 금액 산술의 유일한 코어 (docs/v1.3-design.md §0).
 * 오늘 탭·살까 말까·보류함·위젯이 전부 이 결과를 쓴다.
 */
class CalculateDailyBudgetUseCaseTest {

    private val useCase = CalculateDailyBudgetUseCase(CalculateTodayDefenseLineUseCase())

    private val period = BudgetPeriod(
        startDate = LocalDate.of(2026, 8, 1),
        endDate = LocalDate.of(2026, 8, 31)
    )

    private fun expense(day: Int, amount: Long) = Expense(
        id = day.toLong(),
        title = "지출",
        amount = amount,
        date = LocalDate.of(2026, 8, day),
        type = ExpenseType.GENERAL,
        futureExpenseId = null,
        isEssential = false
    )

    /** 오늘 금고로 옮긴 준비금 — 지출 테이블의 FUTURE_PREPARE 한 줄 */
    private fun prepare(day: Int, amount: Long) = Expense(
        id = 1_000L + day,
        title = "준비",
        amount = amount,
        date = LocalDate.of(2026, 8, day),
        type = ExpenseType.FUTURE_PREPARE,
        futureExpenseId = 1L,
        isEssential = false
    )

    private fun calc(
        today: LocalDate,
        monthlyBudget: Long = 1_000_000L,
        extraIncome: Long = 0L,
        deductions: Long = 0L,
        expenses: List<Expense> = emptyList()
    ) = useCase(
        today = today,
        period = period,
        monthlyBudget = monthlyBudget,
        extraIncomeTotal = extraIncome,
        scheduledDeductionTotal = deductions,
        expenses = expenses
    )

    @Test
    fun `오늘 권장은 오늘 지출을 빼기 전 예산을 남은 일수로 나눈다`() {
        // 8월 22일 → 남은 10일. 예산 100만 − 예정차감 68만 = 32만
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            deductions = 680_000L
        )

        assertEquals(32_000L, r.todayRecommended)
        assertEquals(10, r.remainingDays)
    }

    @Test
    fun `오늘 지출은 오늘 권장을 깎지 않고 남은 금액만 줄인다`() {
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            deductions = 680_000L,
            expenses = listOf(expense(22, 7_400L))
        )

        assertEquals(32_000L, r.todayRecommended)
        assertEquals(7_400L, r.todaySpent)
        assertEquals(24_600L, r.todayLeft)
        assertEquals(312_600L, r.remainingPureBudget)
    }

    @Test
    fun `내일 권장은 오늘 지출까지 뺀 금액을 남은 날에서 하루 뺀 값으로 나눈다`() {
        // 남은 312,600 ÷ 9일
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            deductions = 680_000L,
            expenses = listOf(expense(22, 7_400L))
        )

        assertEquals(34_733L, r.tomorrowRecommended)
    }

    @Test
    fun `초과해도 내일 권장은 조금만 줄어든다 - 재분배`() {
        // 권장 32,000인 날 37,400 사용 → 5,400 초과
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            deductions = 680_000L,
            expenses = listOf(expense(22, 37_400L))
        )

        assertTrue(r.isTodayOver)
        assertEquals(5_400L, r.todayOverAmount)
        // 남은 282,600 ÷ 9일 = 31,400 (오늘 권장보다 600원만 낮다)
        assertEquals(31_400L, r.tomorrowRecommended)
    }

    @Test
    fun `기간 마지막 날에는 내일 권장이 없다`() {
        val r = calc(today = LocalDate.of(2026, 8, 31))

        assertEquals(1, r.remainingDays)
        assertNull(r.tomorrowRecommended)
    }

    @Test
    fun `남은 이틀이면 내일 권장은 남은 금액 전부`() {
        val r = calc(
            today = LocalDate.of(2026, 8, 30),
            monthlyBudget = 50_000L
        )

        assertEquals(2, r.remainingDays)
        assertEquals(50_000L, r.tomorrowRecommended)
    }

    @Test
    fun `과거 지출만 오늘 권장을 깎는다 - 미래 날짜 지출은 제외`() {
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            deductions = 680_000L,
            expenses = listOf(expense(21, 20_000L), expense(25, 50_000L))
        )

        // 300,000 ÷ 10 = 30,000 (25일 지출 5만은 아직 쓴 돈이 아님)
        assertEquals(30_000L, r.todayRecommended)
    }

    @Test
    fun `추가 수익은 예산에 더해진다`() {
        val r = calc(
            today = LocalDate.of(2026, 8, 22),
            monthlyBudget = 300_000L,
            extraIncome = 20_000L
        )

        assertEquals(32_000L, r.todayRecommended)
    }

    // --- 준비금(FUTURE_PREPARE)은 오늘 "쓴 돈"이 아니다 (v1.4.3) ---

    @Test
    fun `오늘 금고에 옮긴 준비금은 오늘 초과로 잡히지 않는다`() {
        // 8/28, 남은 4일, 생활비 400,000 → 권장 100,000. 오늘 300,000 을 금고로.
        val today = LocalDate.of(2026, 8, 28)
        val r = calc(
            today = today,
            monthlyBudget = 400_000L,
            expenses = listOf(prepare(28, 300_000L))
        )

        assertEquals(0L, r.todaySpent)
        assertTrue(!r.isTodayOver)
        // 준비금은 분자에서 빠져 오늘 권장이 낮아진다: (400,000 − 300,000) ÷ 4
        assertEquals(25_000L, r.todayRecommended)
    }

    @Test
    fun `준비금은 남은 생활비에서는 그대로 빠진다 - 이중계산도 누락도 없다`() {
        val today = LocalDate.of(2026, 8, 28)
        val withPrepare = calc(
            today = today,
            monthlyBudget = 400_000L,
            expenses = listOf(prepare(28, 300_000L), expense(28, 10_000L))
        )
        val without = calc(
            today = today,
            monthlyBudget = 400_000L,
            expenses = listOf(expense(28, 10_000L))
        )

        assertEquals(without.remainingPureBudget - 300_000L, withPrepare.remainingPureBudget)
        assertEquals(10_000L, withPrepare.todaySpent)
    }

    @Test
    fun `준비금이 있어도 내일 권장은 준비금을 빼고 나눈 값이다`() {
        // 남은 4일: 오늘 300,000 준비 + 10,000 지출 → 남은 90,000 ÷ 3
        val r = calc(
            today = LocalDate.of(2026, 8, 28),
            monthlyBudget = 400_000L,
            expenses = listOf(prepare(28, 300_000L), expense(28, 10_000L))
        )

        assertEquals(30_000L, r.tomorrowRecommended)
    }

    @Test
    fun `지난 날의 준비금은 지금처럼 과거 지출로 빠진다 - 동작 변화 없음`() {
        val r = calc(
            today = LocalDate.of(2026, 8, 28),
            monthlyBudget = 400_000L,
            expenses = listOf(prepare(20, 100_000L))
        )

        // (400,000 − 100,000) ÷ 4
        assertEquals(75_000L, r.todayRecommended)
        assertEquals(0L, r.todaySpent)
    }
}
