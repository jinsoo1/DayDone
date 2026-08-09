package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseCategory
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.ReportPace
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * 기간 리포트 계산 — 페이스 판정과 결산 모드.
 */
class BuildMonthlyReportUseCaseTest {

    private val useCase = BuildMonthlyReportUseCase(ClassifyExpenseCategoryUseCase())

    // 7월 1~31일, 생활비 300,000원 (수입 1,000,000 - 저축 500,000 - 고정비 200,000)
    private val period = BudgetPeriod(
        startDate = LocalDate.of(2026, 7, 1),
        endDate = LocalDate.of(2026, 7, 31)
    )
    private val income = 1_000_000L
    private val deductions = listOf(
        deduction(1L, "적금", 500_000L, ScheduledDeductionType.SAVING),
        deduction(2L, "월세", 200_000L, ScheduledDeductionType.FIXED)
    )

    private fun deduction(
        id: Long,
        title: String,
        amount: Long,
        type: ScheduledDeductionType
    ) = ScheduledDeduction(
        id = id,
        title = title,
        amount = amount,
        type = type,
        withdrawalDay = 25,
        startYearMonth = YearMonth.of(2026, 1),
        endYearMonth = null,
        memo = null
    )

    private fun expense(
        day: Int,
        amount: Long,
        title: String = "지출",
        type: ExpenseType = ExpenseType.GENERAL,
        isEssential: Boolean = false
    ) = Expense(
        id = day.toLong() * 1000 + amount % 1000,
        title = title,
        amount = amount,
        date = LocalDate.of(2026, 7, day),
        type = type,
        isEssential = isEssential
    )

    private fun report(
        today: LocalDate,
        expenses: List<Expense>
    ) = useCase(period, today, income, deductions, expenses)

    // 지난 기간(6월) — 지난 기간 대비 테스트용
    private val prevPeriod = BudgetPeriod(
        startDate = LocalDate.of(2026, 6, 1),
        endDate = LocalDate.of(2026, 6, 30)
    )

    private fun juneExpense(day: Int, amount: Long) = Expense(
        id = 600L + day,
        title = "지난 지출",
        amount = amount,
        date = LocalDate.of(2026, 6, day),
        type = ExpenseType.GENERAL,
        isEssential = false
    )

    @Test
    fun `기간 절반에 소진율이 낮으면 훌륭한 페이스`() {
        // 16일째(51%)에 30,000원(10%)만 씀
        val result = report(LocalDate.of(2026, 7, 16), listOf(expense(3, 30_000L)))

        assertEquals(ReportPace.GOOD, result.pace)
        assertFalse(result.isFinal)
    }

    @Test
    fun `경과율과 소진율이 비슷하면 정상 페이스`() {
        // 16일째(51%) / 150,000원(50%)
        val result = report(LocalDate.of(2026, 7, 16), listOf(expense(3, 150_000L)))

        assertEquals(ReportPace.ON_TRACK, result.pace)
    }

    @Test
    fun `경과율보다 많이 쓰면 빠른 페이스 - 아직 예산 안`() {
        // 10일째(32%) / 200,000원(67%)
        val result = report(LocalDate.of(2026, 7, 10), listOf(expense(3, 200_000L)))

        assertEquals(ReportPace.FAST, result.pace)
    }

    @Test
    fun `1원이라도 예산을 넘으면 초과로 판정한다`() {
        val result = report(LocalDate.of(2026, 7, 20), listOf(expense(3, 300_001L)))

        assertEquals(ReportPace.OVER, result.pace)
    }

    @Test
    fun `5400원 초과도 초과로 판정한다 - 퍼센트 절삭 회귀 방지`() {
        // 305,400 / 300,000 = 101.8% → 절삭하면 101 이지만 예전엔 100 으로 깎여 FAST 로 빠졌다
        val result = report(LocalDate.of(2026, 7, 20), listOf(expense(3, 305_400L)))

        assertEquals(ReportPace.OVER, result.pace)
        assertTrue("표시 퍼센트도 100을 넘어야 한다", result.spentPercent > 100)
    }

    @Test
    fun `예산의 120퍼센트 이상이면 많은 초과`() {
        val result = report(LocalDate.of(2026, 7, 20), listOf(expense(3, 360_000L)))

        assertEquals(ReportPace.WAY_OVER, result.pace)
    }

    @Test
    fun `기간이 지나면 결산 모드로 계산한다`() {
        val result = report(LocalDate.of(2026, 8, 3), listOf(expense(3, 100_000L)))

        assertTrue(result.isFinal)
        assertEquals(31, result.dayIndex)
        assertEquals(0, result.remainingDays)
        // 결산의 예상 잔액은 실제 잔액
        assertEquals(200_000L, result.projectedLeftover)
    }

    @Test
    fun `무지출 일수는 지난 날만 센다`() {
        // 7월 5일: 확정 대상 1~4일, 2일에만 지출 → 3일
        val result = report(LocalDate.of(2026, 7, 5), listOf(expense(2, 10_000L)))

        assertEquals(3, result.noSpendDays)
    }

    @Test
    fun `기록 시작 전 날들은 무지출로 세지 않고 안내용 날짜를 노출한다`() {
        // 최초 지출이 7월 20일(기간 중간), 오늘 7월 25일 → 확정 대상은 20~24일 (5일), 22일 지출 → 4일
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 25),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(expense(22, 10_000L)),
            firstRecordDate = LocalDate.of(2026, 7, 20)
        )

        assertEquals(4, result.noSpendDays)
        assertEquals(LocalDate.of(2026, 7, 20), result.trackingStartDate)
    }

    @Test
    fun `기록 시작이 기간 시작 전이면 clamp 하지 않고 안내도 없다`() {
        // 6월부터 기록한 유저의 7월 기간 → 기간 시작일부터 그대로 센다
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 5),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = emptyList(),
            firstRecordDate = LocalDate.of(2026, 6, 15)
        )

        assertEquals(4, result.noSpendDays)
        assertEquals(null, result.trackingStartDate)
    }

    @Test
    fun `이전 지출 한 줄 유저는 가입일부터 센다 - 가짜 무지출 방지`() {
        // 7월 20일 가입 + 가입 전 지출 총액을 기간 시작일(1일)에 한 줄로 저장.
        // 오늘 7월 25일 → 가입일(20)~24일 중 22일만 지출 → 무지출 4일 (2~19일은 세지 않음)
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 25),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(
                expense(1, 500_000L, title = "이전 지출"),
                expense(22, 10_000L)
            ),
            firstRecordDate = LocalDate.of(2026, 7, 1),
            firstUseDate = LocalDate.of(2026, 7, 20)
        )

        assertEquals(4, result.noSpendDays)
        assertEquals(LocalDate.of(2026, 7, 20), result.trackingStartDate)
    }

    @Test
    fun `가입일 전에 날짜별 기록이 여러 날 있으면 최초 지출부터 센다`() {
        // 복원 등으로 가입일(26일)이 기록(12일~)보다 늦게 찍힌 경우 — 기록이 진실.
        // 오늘 7월 25일 → 12~24일 중 12·22일 지출 → 무지출 11일
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 25),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(
                expense(12, 50_000L),
                expense(22, 10_000L)
            ),
            firstRecordDate = LocalDate.of(2026, 7, 12),
            firstUseDate = LocalDate.of(2026, 7, 26)
        )

        assertEquals(11, result.noSpendDays)
        assertEquals(LocalDate.of(2026, 7, 12), result.trackingStartDate)
    }

    @Test
    fun `기록 시작이 기간보다 뒤면 clamp 하지 않는다 - 과거 결산 리포트 보호`() {
        // 8월부터 기록한 유저가 7월 결산을 보면 기간 전체 기준으로 계산 (지출 0건, 31일 전부 무지출)
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 8, 10),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = emptyList(),
            firstRecordDate = LocalDate.of(2026, 8, 5)
        )

        assertEquals(null, result.trackingStartDate)
    }

    @Test
    fun `지난 기간 대비는 같은 시점끼리 비교한다`() {
        // 오늘 7/15 (15일째). 지난 기간 지출: 6/10 50,000(시점 안), 6/20 99,999(시점 밖 — 제외돼야 함)
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 15),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(expense(3, 30_000L)),
            firstRecordDate = LocalDate.of(2026, 6, 1),
            previousPeriod = prevPeriod,
            previousExpenses = listOf(juneExpense(10, 50_000L), juneExpense(20, 99_999L))
        )

        val previous = result.previous!!
        // 이번 30,000 − 지난 같은 시점(6/1~6/15) 50,000 = -20,000 (덜 씀)
        assertEquals(-20_000L, previous.spentDiff)
        assertEquals(50_000L / 15, previous.prevDailyAverage)
        // 지난 기간 확정 창 6/1~6/14 (14일) 중 6/10만 지출 → 무지출 13일
        assertEquals(13, previous.prevNoSpendDays)
    }

    @Test
    fun `지난 기간이 부분 기록이면 비교를 숨긴다`() {
        // 기록 시작이 6/5 — 지난 기간 중간부터라 비교하면 어긋난 비교가 된다
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 7, 15),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(expense(3, 30_000L)),
            firstRecordDate = LocalDate.of(2026, 6, 5),
            previousPeriod = prevPeriod,
            previousExpenses = listOf(juneExpense(10, 50_000L))
        )

        assertEquals(null, result.previous)
    }

    @Test
    fun `결산 리포트의 지난 기간 대비는 기간 전체끼리 비교한다`() {
        // 오늘 8/5 → 7월 결산 (dayIndex=31). 지난 기간(6월) 전체 지출과 비교
        val result = useCase(
            period = period,
            today = LocalDate.of(2026, 8, 5),
            totalAvailableBudget = income,
            deductions = deductions,
            expenses = listOf(expense(3, 100_000L)),
            firstRecordDate = LocalDate.of(2026, 6, 1),
            previousPeriod = prevPeriod,
            previousExpenses = listOf(juneExpense(10, 50_000L), juneExpense(20, 30_000L))
        )

        val previous = result.previous!!
        assertEquals(100_000L - 80_000L, previous.spentDiff)
        // 6월 30일 전부 확정, 지출 2일 → 무지출 28일
        assertEquals(28, previous.prevNoSpendDays)
    }

    @Test
    fun `준비금은 소진율에는 포함되지만 카테고리 집계에서는 빠진다`() {
        val expenses = listOf(
            expense(2, 50_000L, title = "커피"),
            expense(3, 100_000L, title = "자동차세 준비", type = ExpenseType.FUTURE_PREPARE)
        )

        val result = report(LocalDate.of(2026, 7, 10), expenses)

        // 150,000 / 300,000 = 50%
        assertEquals(50, result.spentPercent)
        // 카테고리는 일반 지출만
        assertEquals(1, result.categories.size)
        assertEquals(ExpenseCategory.CAFE, result.categories.single().category)
    }

    @Test
    fun `카테고리는 금액순으로 묶이고 같은 이름은 합산된다`() {
        val expenses = listOf(
            expense(2, 5_000L, title = "커피"),
            expense(3, 4_500L, title = "커피"),
            expense(4, 9_000L, title = "스타벅스"),
            expense(5, 30_000L, title = "점심")
        )

        val result = report(LocalDate.of(2026, 7, 10), expenses)

        // 식비 30,000 > 카페 18,500
        assertEquals(ExpenseCategory.FOOD, result.categories[0].category)
        assertEquals(ExpenseCategory.CAFE, result.categories[1].category)

        val cafe = result.categories[1]
        assertEquals(3, cafe.count)
        assertEquals(18_500L, cafe.total)
        // 커피 2건이 하나로 합산
        assertEquals(9_500L, cafe.items.first { it.title == "커피" }.total)
        assertEquals(2, cafe.items.first { it.title == "커피" }.count)
    }

    @Test
    fun `필수 비중과 고정지출 비율을 계산한다`() {
        val expenses = listOf(
            expense(2, 60_000L, isEssential = true),
            expense(3, 40_000L, isEssential = false)
        )

        val result = report(LocalDate.of(2026, 7, 10), expenses)

        assertEquals(60, result.essentialPercent)
        // (500,000 + 200,000) / 1,000,000
        assertEquals(70, result.deductionPercent)
        assertEquals(50, result.savingPercent)
    }

    @Test
    fun `초과하면 초과 제안이 가장 먼저 나온다`() {
        val result = report(LocalDate.of(2026, 7, 20), listOf(expense(3, 400_000L, title = "점심")))

        assertTrue(result.suggestions.isNotEmpty())
        assertTrue(
            "초과 금액이 제안에 들어가야 한다",
            result.suggestions.first().text.contains("100,000")
        )
    }

    @Test
    fun `제안은 항상 하나 이상 나온다`() {
        val result = report(LocalDate.of(2026, 7, 2), emptyList())

        assertTrue(result.suggestions.isNotEmpty())
    }
}
