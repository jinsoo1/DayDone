package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.DailyBudgetSnapshot
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.time.YearMonth

/**
 * 오늘 기준 생활비 스냅샷을 DB에서 직접 읽어 흘려보낸다.
 *
 * 데이터를 이미 들고 있는 오늘 탭은 이걸 쓰지 않고 [CalculateDailyBudgetUseCase]를 직접 부른다.
 * 이 UseCase는 **앱 화면 밖**(보류함 재계산, 홈 위젯)에서 쓴다.
 * 산술은 전부 [CalculateDailyBudgetUseCase]에 있으므로 두 경로의 숫자는 항상 같다.
 */
class ObserveDailyBudgetUseCase @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val observeEffectiveMonthlyBudgetUseCase: ObserveEffectiveMonthlyBudgetUseCase,
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val observeExtraIncomesByPeriodUseCase: ObserveExtraIncomesByPeriodUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase,
    private val calculateDailyBudgetUseCase: CalculateDailyBudgetUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(today: LocalDate): Flow<DailyBudgetSnapshot> {
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

                calculateDailyBudgetUseCase(
                    today = today,
                    period = period,
                    monthlyBudget = monthlyBudget,
                    extraIncomeTotal = extraIncomes.sumOf { it.amount },
                    scheduledDeductionTotal = deductionTotal,
                    expenses = expenses
                )
            }
        }
    }
}
