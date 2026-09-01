package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.model.LedgerEntryKind
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * 기간 내역(장부) 묶기 — 날짜별 그룹·합계·정렬.
 * 새 계산이 아니라 기존 기록을 모으기만 하므로, 합계는 항목 합과 정확히 일치해야 한다.
 */
class BuildPeriodLedgerUseCaseTest {

    private val useCase = BuildPeriodLedgerUseCase(GetDeductionsDueOnUseCase())

    private val period = BudgetPeriod(
        startDate = LocalDate.of(2026, 9, 1),
        endDate = LocalDate.of(2026, 9, 30)
    )

    private fun expense(
        id: Long,
        day: Int,
        amount: Long,
        title: String = "지출$id",
        type: ExpenseType = ExpenseType.GENERAL,
        essential: Boolean = false
    ) = Expense(
        id = id,
        title = title,
        amount = amount,
        date = LocalDate.of(2026, 9, day),
        type = type,
        isEssential = essential
    )

    private fun income(id: Long, day: Int, amount: Long) = ExtraIncome(
        id = id,
        title = "수익$id",
        amount = amount,
        date = LocalDate.of(2026, 9, day),
        memo = null
    )

    private fun deduction(
        id: Long,
        withdrawalDay: Int,
        amount: Long,
        type: ScheduledDeductionType = ScheduledDeductionType.FIXED
    ) = ScheduledDeduction(
        id = id,
        title = "차감$id",
        amount = amount,
        type = type,
        withdrawalDay = withdrawalDay,
        startYearMonth = YearMonth.of(2026, 1),
        endYearMonth = null,
        memo = null
    )

    @Test
    fun `날짜별로 묶고 그날 합계를 낸다`() {
        val ledger = useCase(
            period = period,
            expenses = listOf(
                expense(1, day = 1, amount = 5_000),
                expense(2, day = 1, amount = 3_000),
                expense(3, day = 5, amount = 12_000)
            ),
            extraIncomes = listOf(income(1, day = 5, amount = 100_000)),
            deductions = emptyList(),
            ascending = true
        )

        assertEquals(2, ledger.days.size)

        val first = ledger.days[0]
        assertEquals(LocalDate.of(2026, 9, 1), first.date)
        assertEquals(8_000L, first.spent)
        assertEquals(0L, first.income)
        assertEquals(2, first.entries.size)

        val second = ledger.days[1]
        assertEquals(LocalDate.of(2026, 9, 5), second.date)
        assertEquals(12_000L, second.spent)
        assertEquals(100_000L, second.income)

        assertEquals(20_000L, ledger.spentTotal)
        assertEquals(100_000L, ledger.incomeTotal)
    }

    @Test
    fun `기록 없는 날은 만들지 않는다`() {
        val ledger = useCase(
            period = period,
            expenses = listOf(expense(1, day = 10, amount = 1_000)),
            extraIncomes = emptyList(),
            deductions = emptyList(),
            ascending = true
        )

        assertEquals(1, ledger.days.size)
        assertEquals(LocalDate.of(2026, 9, 10), ledger.days.single().date)
    }

    @Test
    fun `내림차순은 마지막 날부터 나오고 합계는 그대로다`() {
        val expenses = listOf(
            expense(1, day = 1, amount = 5_000),
            expense(2, day = 20, amount = 7_000)
        )

        val asc = useCase(period, expenses, emptyList(), emptyList(), ascending = true)
        val desc = useCase(period, expenses, emptyList(), emptyList(), ascending = false)

        assertEquals(listOf(1, 20), asc.days.map { it.date.dayOfMonth })
        assertEquals(listOf(20, 1), desc.days.map { it.date.dayOfMonth })
        assertEquals(asc.spentTotal, desc.spentTotal)
    }

    @Test
    fun `하루 안 순서는 수입 다음에 지출 그다음 저축·고정비`() {
        val ledger = useCase(
            period = period,
            expenses = listOf(
                expense(1, day = 25, amount = 5_000),
                expense(2, day = 25, amount = 30_000, type = ExpenseType.FUTURE_PREPARE)
            ),
            extraIncomes = listOf(income(1, day = 25, amount = 50_000)),
            deductions = listOf(
                deduction(1, withdrawalDay = 25, amount = 500_000, type = ScheduledDeductionType.SAVING)
            ),
            ascending = true
        )

        assertEquals(
            listOf(
                LedgerEntryKind.EXTRA_INCOME,
                LedgerEntryKind.EXPENSE,
                LedgerEntryKind.FUTURE_PREPARE,
                LedgerEntryKind.SAVING
            ),
            ledger.days.single().entries.map { it.kind }
        )

        val day = ledger.days.single()
        // 준비금은 지출에 포함, 저축·고정비는 따로 센다 (월 탭 요약과 같은 구분)
        assertEquals(35_000L, day.spent)
        assertEquals(500_000L, day.deducted)
        assertEquals(50_000L, day.income)
    }

    @Test
    fun `출금일 31은 그 달 말일로 잡힌다`() {
        val ledger = useCase(
            period = BudgetPeriod(
                startDate = LocalDate.of(2026, 2, 1),
                endDate = LocalDate.of(2026, 2, 28)
            ),
            expenses = emptyList(),
            extraIncomes = emptyList(),
            deductions = listOf(deduction(1, withdrawalDay = 31, amount = 100_000)),
            ascending = true
        )

        assertEquals(LocalDate.of(2026, 2, 28), ledger.days.single().date)
        assertEquals(100_000L, ledger.deductedTotal)
    }

    @Test
    fun `기간 밖 지출은 담지 않는다`() {
        val ledger = useCase(
            period = period,
            expenses = listOf(
                expense(1, day = 1, amount = 5_000),
                Expense(
                    id = 2,
                    title = "지난 기간",
                    amount = 9_000,
                    date = LocalDate.of(2026, 8, 31),
                    type = ExpenseType.GENERAL
                )
            ),
            extraIncomes = emptyList(),
            deductions = emptyList(),
            ascending = true
        )

        assertEquals(1, ledger.days.size)
        assertEquals(5_000L, ledger.spentTotal)
        assertTrue(ledger.days.none { it.date.monthValue == 8 })
    }

    @Test
    fun `필수 지출 표시는 항목에 그대로 남는다`() {
        val ledger = useCase(
            period = period,
            expenses = listOf(expense(1, day = 3, amount = 4_000, essential = true)),
            extraIncomes = emptyList(),
            deductions = emptyList(),
            ascending = true
        )

        assertTrue(ledger.days.single().entries.single().isEssential)
    }
}
