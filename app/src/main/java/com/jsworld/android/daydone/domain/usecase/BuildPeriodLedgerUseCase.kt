package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExpenseType
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.model.LedgerDay
import com.jsworld.android.daydone.domain.model.LedgerEntry
import com.jsworld.android.daydone.domain.model.LedgerEntryKind
import com.jsworld.android.daydone.domain.model.PeriodLedger
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import jakarta.inject.Inject

/**
 * 한 예산 기간의 내역을 **날짜별로 묶어** 한 줄씩 이어보게 만든다 (내역 화면).
 *
 * 순수 함수 — 새 계산을 만들지 않고 이미 있는 기록만 모은다.
 * 하루 안 순서는 **수입 → 지출·준비금 → 저축·고정비**.
 *
 * @param deductions 이 기간용으로 이미 걸러지고 금액이 확정된 목록
 *   ([GetScheduledDeductionsInPeriodUseCase] + [ResolveScheduledDeductionAmountsUseCase] 통과)
 * @param ascending true = 기간 첫날부터, false = 마지막 날부터
 */
class BuildPeriodLedgerUseCase @Inject constructor(
    private val getDeductionsDueOnUseCase: GetDeductionsDueOnUseCase
) {
    operator fun invoke(
        period: BudgetPeriod,
        expenses: List<Expense>,
        extraIncomes: List<ExtraIncome>,
        deductions: List<ScheduledDeduction>,
        ascending: Boolean
    ): PeriodLedger {
        val expensesByDate = expenses
            .filter { period.contains(it.date) }
            .groupBy { it.date }

        val incomesByDate = extraIncomes
            .filter { period.contains(it.date) }
            .groupBy { it.date }

        val days = mutableListOf<LedgerDay>()

        var cursor = period.startDate
        while (!cursor.isAfter(period.endDate)) {
            val dayExpenses = expensesByDate[cursor].orEmpty()
            val dayIncomes = incomesByDate[cursor].orEmpty()
            val dayDeductions = getDeductionsDueOnUseCase(deductions, cursor)

            if (dayExpenses.isNotEmpty() || dayIncomes.isNotEmpty() || dayDeductions.isNotEmpty()) {
                days.add(
                    LedgerDay(
                        date = cursor,
                        spent = dayExpenses.sumOf { it.amount },
                        income = dayIncomes.sumOf { it.amount },
                        deducted = dayDeductions.sumOf { it.amount },
                        entries = buildEntries(dayIncomes, dayExpenses, dayDeductions)
                    )
                )
            }

            cursor = cursor.plusDays(1)
        }

        return PeriodLedger(
            days = if (ascending) days else days.reversed(),
            spentTotal = days.sumOf { it.spent },
            incomeTotal = days.sumOf { it.income },
            deductedTotal = days.sumOf { it.deducted }
        )
    }

    private fun buildEntries(
        incomes: List<ExtraIncome>,
        expenses: List<Expense>,
        deductions: List<ScheduledDeduction>
    ): List<LedgerEntry> = buildList {
        incomes.sortedBy { it.id }.forEach {
            add(
                LedgerEntry(
                    kind = LedgerEntryKind.EXTRA_INCOME,
                    title = it.title,
                    amount = it.amount,
                    extraIncomeId = it.id
                )
            )
        }

        expenses.sortedBy { it.id }.forEach {
            add(
                LedgerEntry(
                    kind = if (it.type == ExpenseType.FUTURE_PREPARE) {
                        LedgerEntryKind.FUTURE_PREPARE
                    } else {
                        LedgerEntryKind.EXPENSE
                    },
                    title = it.title,
                    amount = it.amount,
                    isEssential = it.isEssential,
                    expenseId = it.id
                )
            )
        }

        deductions.sortedBy { it.id }.forEach {
            add(
                LedgerEntry(
                    kind = if (it.type == ScheduledDeductionType.SAVING) {
                        LedgerEntryKind.SAVING
                    } else {
                        LedgerEntryKind.FIXED
                    },
                    title = it.title,
                    amount = it.amount,
                    deductionId = it.id
                )
            )
        }
    }
}
