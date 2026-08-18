package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.PureBudgetSnapshot
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 오늘 기준 "남은 순수 생활비"와 "남은 일수"를 관찰한다.
 *
 * ⚠️ TodayViewModel.loadToday 와 반드시 같은 값이 나와야 한다 (같은 소스·같은 식):
 *   남은 순수 생활비 = 유효 월 예산 + 추가수익 − 저축/고정비(이월 반영, 기간 내)
 *                     − 오늘까지의 지출 합 (미래 날짜 지출은 제외 — 오늘 탭과 동일)
 * 살까 말까를 오늘 탭 밖(보류함)에서 재계산할 때 쓴다. 숫자가 오늘 탭과 1원이라도
 * 다르면 신뢰가 깨지므로, 계산식을 바꿀 땐 양쪽을 함께 바꿀 것.
 */
class ObservePureBudgetUseCase @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val observeEffectiveMonthlyBudgetUseCase: ObserveEffectiveMonthlyBudgetUseCase,
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val observeExtraIncomesByPeriodUseCase: ObserveExtraIncomesByPeriodUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(today: LocalDate): Flow<PureBudgetSnapshot> {
        return observeBudgetProfileUseCase().flatMapLatest { profile ->
            val period = getCurrentBudgetPeriodUseCase(
                today = today,
                budgetStartDay = profile.budgetStartDay
            )
            val anchorMonth = YearMonth.from(period.startDate)

            combine(
                observeEffectiveMonthlyBudgetUseCase(
                    anchorMonth = anchorMonth,
                    default = profile.monthlyIncome
                ),
                observeExpensesByPeriodUseCase(
                    startDate = period.startDate,
                    endDate = period.endDate
                ),
                observeExtraIncomesByPeriodUseCase(
                    startDate = period.startDate,
                    endDate = period.endDate
                ),
                observeScheduledDeductionsUseCase(),
                observeScheduledDeductionAmountsUseCase()
            ) { monthlyBudget, expenses, extraIncomes, deductions, overrides ->
                val deductionTotal = resolveScheduledDeductionAmountsUseCase(
                    deductions = getScheduledDeductionsInPeriodUseCase(
                        deductions = deductions,
                        budgetPeriod = period
                    ),
                    overrides = overrides,
                    anchorMonth = anchorMonth
                ).sumOf { it.amount }

                val spentUntilToday = expenses
                    .filter { !it.date.isAfter(today) }
                    .sumOf { it.amount }

                val remaining = monthlyBudget +
                        extraIncomes.sumOf { it.amount } -
                        deductionTotal -
                        spentUntilToday

                val remainingDays = ChronoUnit.DAYS.between(today, period.endDate)
                    .toInt() + 1

                PureBudgetSnapshot(
                    remainingPureBudget = remaining,
                    remainingDays = remainingDays
                )
            }
        }
    }
}
