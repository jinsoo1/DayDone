package com.jsworld.android.daydone.presentation.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.MonthlyReport
import com.jsworld.android.daydone.domain.usecase.BuildMonthlyReportUseCase
import com.jsworld.android.daydone.domain.usecase.GetBudgetPeriodForMonthUseCase
import com.jsworld.android.daydone.domain.usecase.GetFirstExpenseDateUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.GetScheduledDeductionsInPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveEffectiveMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExpensesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExtraIncomesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionsUseCase
import com.jsworld.android.daydone.domain.usecase.ResolveScheduledDeductionAmountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.YearMonth

data class ReportUiState(
    val isLoading: Boolean = true,
    val report: MonthlyReport? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val getBudgetPeriodForMonthUseCase: GetBudgetPeriodForMonthUseCase,
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val observeExtraIncomesByPeriodUseCase: ObserveExtraIncomesByPeriodUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val observeEffectiveMonthlyBudgetUseCase: ObserveEffectiveMonthlyBudgetUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase,
    private val getFirstExpenseDateUseCase: GetFirstExpenseDateUseCase,
    private val buildMonthlyReportUseCase: BuildMonthlyReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // 결산 리포트 진입 시 대상 기간의 anchorMonth("yyyy-MM"). null이면 현재 기간.
    private val targetMonth: YearMonth? =
        savedStateHandle.get<String>("month")
            ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }

    init {
        observeReport()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeReport() {
        observeBudgetProfileUseCase()
            .flatMapLatest { profile ->
                val today = LocalDate.now()
                val period = if (targetMonth != null) {
                    getBudgetPeriodForMonthUseCase(
                        yearMonth = targetMonth,
                        budgetStartDay = profile.budgetStartDay
                    )
                } else {
                    getCurrentBudgetPeriodUseCase(
                        today = today,
                        budgetStartDay = profile.budgetStartDay
                    )
                }
                val anchorMonth = YearMonth.from(period.startDate)
                val firstRecordDate = getFirstExpenseDateUseCase()
                // 지난 기간 대비 비교용 — 지난 기간의 지출도 함께 관찰한다
                val previousPeriod = getBudgetPeriodForMonthUseCase(
                    yearMonth = anchorMonth.minusMonths(1),
                    budgetStartDay = profile.budgetStartDay
                )

                combine(
                    combine(
                        observeExpensesByPeriodUseCase(
                            startDate = period.startDate,
                            endDate = period.endDate
                        ),
                        observeExpensesByPeriodUseCase(
                            startDate = previousPeriod.startDate,
                            endDate = previousPeriod.endDate
                        )
                    ) { current, prev -> current to prev },
                    observeExtraIncomesByPeriodUseCase(
                        startDate = period.startDate,
                        endDate = period.endDate
                    ),
                    observeScheduledDeductionsUseCase(),
                    observeScheduledDeductionAmountsUseCase(),
                    observeEffectiveMonthlyBudgetUseCase(
                        anchorMonth = anchorMonth,
                        default = profile.monthlyIncome
                    )
                ) { expensesPair, extraIncomes, deductions, overrides, monthlyBudget ->
                    val (expenses, previousExpenses) = expensesPair
                    val deductionsInPeriod = resolveScheduledDeductionAmountsUseCase(
                        deductions = getScheduledDeductionsInPeriodUseCase(
                            deductions = deductions,
                            budgetPeriod = period
                        ),
                        overrides = overrides,
                        anchorMonth = anchorMonth
                    )

                    buildMonthlyReportUseCase(
                        period = period,
                        today = today,
                        totalAvailableBudget = monthlyBudget + extraIncomes.sumOf { it.amount },
                        deductions = deductionsInPeriod,
                        expenses = expenses,
                        firstRecordDate = firstRecordDate,
                        firstUseDate = profile.firstUseDate,
                        previousPeriod = previousPeriod,
                        previousExpenses = previousExpenses
                    )
                }
            }
            .onEach { report ->
                _uiState.value = ReportUiState(isLoading = false, report = report)
            }
            .launchIn(viewModelScope)
    }
}
