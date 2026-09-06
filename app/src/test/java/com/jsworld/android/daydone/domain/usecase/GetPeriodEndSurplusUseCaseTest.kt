package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * 기간 막바지 여유분 안내 — 조건이 좁아야 잔소리가 안 된다.
 * 예상 잔액은 리포트의 projectedLeftover 와 **같은 숫자**여야 한다(두 화면이 다른 말을 하면 안 됨).
 */
class GetPeriodEndSurplusUseCaseTest {

    private val useCase = GetPeriodEndSurplusUseCase()

    // 30일 기간, 순수 생활비 1,500,000 (하루 50,000 페이스)
    private val period = BudgetPeriod(
        startDate = LocalDate.of(2026, 9, 1),
        endDate = LocalDate.of(2026, 9, 30)
    )
    private val totalPure = 1_500_000L

    @Test
    fun `마지막 5일 안이고 10% 이상 남으면 예상 잔액을 돌려준다`() {
        // 26일째, 남은 5일, 1,000,000 씀 → 남은 500,000
        val result = useCase(
            remaining = 500_000,
            generalSpent = 1_000_000,
            totalPureBudget = totalPure,
            dayIndex = 26,
            remainingDays = 5
        )

        assertEquals(500_000L - (1_000_000L / 26) * 5, result)
    }

    @Test
    fun `남은 6일이면 아직 안내하지 않는다 - LATE_DAYS 경계 바깥`() {
        assertNull(
            useCase(
                remaining = 900_000,
                generalSpent = 600_000,
                totalPureBudget = totalPure,
                dayIndex = 25,
                remainingDays = 6
            )
        )
    }

    @Test
    fun `남은 5일이면 안내한다 - LATE_DAYS 경계 포함`() {
        val result = useCase(
            remaining = 900_000,
            generalSpent = 600_000,
            totalPureBudget = totalPure,
            dayIndex = 26,
            remainingDays = 5
        )

        assertEquals(900_000L - (600_000L / 26) * 5, result)
    }

    @Test
    fun `예상 잔액이 10%에 못 미치면 안내하지 않는다`() {
        // 하루 평균 50,000 × 5일 = 250,000 → 잔액 0
        assertNull(
            useCase(
                remaining = 250_000,
                generalSpent = 1_250_000,
                totalPureBudget = totalPure,
                dayIndex = 25,
                remainingDays = 5
            )
        )
    }

    @Test
    fun `정확히 10%면 안내한다 - 경계 포함`() {
        // 하루 평균 (28,000/28)=1,000 × 3일 = 3,000 → 153,000 − 3,000 = 150,000 = 10%
        val result = useCase(
            remaining = 153_000,
            generalSpent = 28_000,
            totalPureBudget = totalPure,
            dayIndex = 28,
            remainingDays = 3
        )

        assertEquals(150_000L, result)
    }

    @Test
    fun `초과 상태면 안내하지 않는다`() {
        assertNull(
            useCase(
                remaining = -30_000,
                generalSpent = 1_530_000,
                totalPureBudget = totalPure,
                dayIndex = 28,
                remainingDays = 3
            )
        )
    }

    @Test
    fun `이번 기간에 적은 지출이 없으면 안내하지 않는다 - 안 쓴 게 아니라 안 적은 것일 수 있다`() {
        assertNull(
            useCase(
                remaining = totalPure,
                generalSpent = 0,
                totalPureBudget = totalPure,
                dayIndex = 28,
                remainingDays = 3
            )
        )
    }

    @Test
    fun `기간 마지막 날에도 계산된다 - 남은 1일`() {
        val result = useCase(
            remaining = 400_000,
            generalSpent = 1_200_000,
            totalPureBudget = totalPure,
            dayIndex = 30,
            remainingDays = 1
        )

        assertEquals(400_000L - (1_200_000L / 30), result)
    }

    @Test
    fun `총 생활비가 0 이하이면 안내하지 않는다`() {
        assertNull(
            useCase(
                remaining = 100_000,
                generalSpent = 50_000,
                totalPureBudget = 0,
                dayIndex = 28,
                remainingDays = 3
            )
        )
    }

    // --- 리포트와 같은 숫자인지: 식을 베껴 적지 않고 리포트를 실제로 돌려 맞춰본다 ---

    private val reportUseCase = BuildMonthlyReportUseCase(ClassifyExpenseCategoryUseCase())

    private fun expense(day: Int, amount: Long, type: ExpenseType = ExpenseType.GENERAL) = Expense(
        id = day.toLong() * 10 + amount % 7,
        title = "지출",
        amount = amount,
        date = LocalDate.of(2026, 9, day),
        type = type,
        futureExpenseId = null,
        isEssential = false
    )

    private val deductions = listOf(
        ScheduledDeduction(
            id = 1,
            title = "적금",
            amount = 500_000,
            type = ScheduledDeductionType.SAVING,
            withdrawalDay = 10,
            startYearMonth = YearMonth.of(2026, 1),
            endYearMonth = null,
            memo = null
        )
    )

    /** 순수 생활비 = 예산 2,000,000 − 저축 500,000 = 1,500,000 */
    private fun surplusFor(today: LocalDate, expenses: List<Expense>): Long? {
        val dayIndex = today.dayOfMonth
        return useCase(
            remaining = totalPure - expenses.sumOf { it.amount },
            generalSpent = expenses.filter { it.type == ExpenseType.GENERAL }.sumOf { it.amount },
            totalPureBudget = totalPure,
            dayIndex = dayIndex,
            remainingDays = period.endDate.dayOfMonth - dayIndex + 1
        )
    }

    private fun reportLeftoverFor(today: LocalDate, expenses: List<Expense>): Long =
        reportUseCase(
            period = period,
            today = today,
            totalAvailableBudget = 2_000_000,
            deductions = deductions,
            expenses = expenses
        ).projectedLeftover

    @Test
    fun `리포트 예상 잔액과 같은 숫자다`() {
        val today = LocalDate.of(2026, 9, 27)
        val expenses = (1..27).map { expense(it, 38_000) }

        assertEquals(reportLeftoverFor(today, expenses), surplusFor(today, expenses))
    }

    @Test
    fun `미래 날짜에 미리 적어둔 지출이 있어도 리포트와 같은 숫자다`() {
        // 27일째인데 30일에 나갈 저녁값을 미리 적어둔 유저.
        // 오늘 탭 기준(미래 날짜 제외)으로 계산하면 이 200,000 을 "남는 돈"으로 세어
        // 리포트보다 큰 숫자를 말하게 된다 — 그 회귀를 막는 테스트.
        val today = LocalDate.of(2026, 9, 27)
        // 임계(10%)는 넘는 규모로 잡아 둘 다 같은 금액을 내는지 본다
        val expenses = (1..27).map { expense(it, 30_000) } + expense(30, 200_000)

        assertEquals(reportLeftoverFor(today, expenses), surplusFor(today, expenses))
    }

    @Test
    fun `준비금은 남은 돈에서 빠지지만 하루 평균에는 안 들어간다 - 리포트와 동일`() {
        val today = LocalDate.of(2026, 9, 28)
        val expenses = (1..28).map { expense(it, 30_000) } +
                expense(20, 150_000, type = ExpenseType.FUTURE_PREPARE)

        assertEquals(reportLeftoverFor(today, expenses), surplusFor(today, expenses))
    }
}
