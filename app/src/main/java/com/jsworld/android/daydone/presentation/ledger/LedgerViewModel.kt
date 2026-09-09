package com.jsworld.android.daydone.presentation.ledger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.model.LedgerEntry
import com.jsworld.android.daydone.domain.model.LedgerEntryKind
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.usecase.BuildPeriodLedgerUseCase
import com.jsworld.android.daydone.domain.usecase.GetBudgetPeriodForMonthUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.GetScheduledDeductionsInPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExpensesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveEffectiveMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExtraIncomesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveLedgerDeductionsVisibleUseCase
import com.jsworld.android.daydone.domain.usecase.SetLedgerDeductionsVisibleUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionsUseCase
import com.jsworld.android.daydone.domain.usecase.ResolveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.presentation.ledger.model.LedgerDayUiModel
import com.jsworld.android.daydone.presentation.ledger.model.LedgerEntryUiModel
import com.jsworld.android.daydone.presentation.ledger.model.LedgerUiState
import com.jsworld.android.daydone.presentation.util.toMoneyText
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * 기간 내역 화면 — 한 예산 기간의 수입·지출을 날짜별로 묶어 쭉 보여준다.
 *
 * 읽기 전용(파생)이라 새 저장 경로가 없다. 수정은 월 탭 캘린더의 그 날짜에서 한다.
 * 보는 기간은 진입할 때 넘어온 `month`(anchorMonth) 하나로 고정 — 월 이동은 월 탭에서.
 */
@HiltViewModel
class LedgerViewModel @Inject constructor(
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
    private val buildPeriodLedgerUseCase: BuildPeriodLedgerUseCase,
    private val observeLedgerDeductionsVisibleUseCase: ObserveLedgerDeductionsVisibleUseCase,
    private val setLedgerDeductionsVisibleUseCase: SetLedgerDeductionsVisibleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    /** 진입 시 넘어온 대상 기간의 anchorMonth("yyyy-MM"). null이면 현재 기간. */
    private val targetMonth: YearMonth? =
        savedStateHandle.get<String>("month")
            ?.let { runCatching { YearMonth.parse(it) }.getOrNull() }

    /** 정렬만 바꿀 때 DB 를 다시 읽지 않도록 조회 흐름 밖에 둔다. */
    private val ascending = MutableStateFlow(true)

    init {
        observeLedger()
    }

    fun onToggleSort() {
        ascending.value = !ascending.value
    }

    /** 목록에서 저축·고정비 줄 보이기/숨기기 — 기기에 저장돼 다음에 열어도 유지된다. */
    fun onToggleDeductions() {
        val next = !_uiState.value.showDeductions
        viewModelScope.launch { setLedgerDeductionsVisibleUseCase(next) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeLedger() {
        observeBudgetProfileUseCase()
            .flatMapLatest { profile ->
                val period = targetMonth?.let {
                    getBudgetPeriodForMonthUseCase(
                        yearMonth = it,
                        budgetStartDay = profile.budgetStartDay
                    )
                } ?: getCurrentBudgetPeriodUseCase(
                    today = LocalDate.now(),
                    budgetStartDay = profile.budgetStartDay
                )

                val anchorMonth = YearMonth.from(period.startDate)

                combine(
                    observeExpensesByPeriodUseCase(
                        startDate = period.startDate,
                        endDate = period.endDate
                    ),
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
                ) { expenses, extraIncomes, deductions, overrides, monthlyBudget ->
                    LedgerData(
                        period = period,
                        monthlyBudget = monthlyBudget,
                        expenses = expenses,
                        extraIncomes = extraIncomes,
                        deductions = resolveScheduledDeductionAmountsUseCase(
                            deductions = getScheduledDeductionsInPeriodUseCase(
                                deductions = deductions,
                                budgetPeriod = period
                            ),
                            overrides = overrides,
                            anchorMonth = anchorMonth
                        )
                    )
                }
            }
            .combine(ascending) { data, asc -> data to asc }
            .combine(observeLedgerDeductionsVisibleUseCase()) { (data, asc), showDeductions ->
                Triple(data, asc, showDeductions)
            }
            .onEach { (data, asc, showDeductions) -> render(data, asc, showDeductions) }
            .launchIn(viewModelScope)
    }

    private fun render(data: LedgerData, ascending: Boolean, showDeductions: Boolean) {
        val ledger = buildPeriodLedgerUseCase(
            period = data.period,
            expenses = data.expenses,
            extraIncomes = data.extraIncomes,
            deductions = data.deductions,
            ascending = ascending
        )

        // 저축·고정비 숨김은 **목록만** 거른다. 합계(deductedTotal·remaining)는 ledger 그대로 —
        // 안 보이게 했다고 남은 돈이 달라지면 안 된다. 그 줄만 있던 날은 카드 자체를 만들지 않는다.
        val visibleDays = if (showDeductions) {
            ledger.days
        } else {
            ledger.days.mapNotNull { day ->
                val kept = day.entries.filter {
                    it.kind != LedgerEntryKind.SAVING && it.kind != LedgerEntryKind.FIXED
                }
                if (kept.isEmpty()) null else day.copy(entries = kept, deducted = 0L)
            }
        }

        val today = LocalDate.now()
        val anchorMonth = YearMonth.from(data.period.startDate)

        _uiState.value = LedgerUiState(
            isLoading = false,
            monthTitle = "${anchorMonth.year}년 ${anchorMonth.monthValue}월 내역",
            periodText = "${data.period.startDate.monthValue}월 " +
                    "${data.period.startDate.dayOfMonth}일 ~ " +
                    "${data.period.endDate.monthValue}월 " +
                    "${data.period.endDate.dayOfMonth}일",
            ascending = ascending,
            showDeductions = showDeductions,
            monthlyBudget = data.monthlyBudget,
            incomeTotal = ledger.incomeTotal,
            spentTotal = ledger.spentTotal,
            deductedTotal = ledger.deductedTotal,
            // 월 탭 예산 요약과 **같은 식** — 숫자가 탭마다 다르면 신뢰가 깨진다.
            // (deductedTotal 은 GetScheduledDeductionsInPeriodUseCase 와 같은 출금일 판정을
            //  쓰므로 월 탭의 저축+고정비 합계와 항상 같다)
            remaining = data.monthlyBudget + ledger.incomeTotal -
                    ledger.deductedTotal - ledger.spentTotal,
            isCurrentPeriod = !today.isBefore(data.period.startDate) &&
                    !today.isAfter(data.period.endDate),
            days = visibleDays.mapIndexed { index, day ->
                val previousMonth = visibleDays.getOrNull(index - 1)?.date?.monthValue

                LedgerDayUiModel(
                    date = day.date,
                    dayText = day.date.dayOfMonth.toString(),
                    weekText = day.date.toKoreanWeek(),
                    // 기간이 두 달에 걸칠 때(시작일 ≠ 1일) 달이 바뀌는 지점만 표시
                    monthLabel = if (day.date.monthValue != previousMonth) {
                        "${day.date.monthValue}월"
                    } else {
                        null
                    },
                    isToday = day.date == today,
                    spent = day.spent,
                    income = day.income,
                    deducted = day.deducted,
                    entries = day.entries.map { it.toUiModel() }
                )
            }
        )
    }

    private fun LedgerEntry.toUiModel(): LedgerEntryUiModel {
        val isIncome = kind == LedgerEntryKind.EXTRA_INCOME

        return LedgerEntryUiModel(
            kind = kind,
            title = title,
            amountText = (if (isIncome) "+" else "−") + amount.toMoneyText(),
            tag = when {
                kind == LedgerEntryKind.FUTURE_PREPARE -> "준비금"
                kind == LedgerEntryKind.SAVING -> "저축"
                kind == LedgerEntryKind.FIXED -> "고정비"
                kind == LedgerEntryKind.EXPENSE && isEssential -> "필수"
                else -> null
            }
        )
    }

    private fun LocalDate.toKoreanWeek(): String =
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREA)
}

private data class LedgerData(
    val period: BudgetPeriod,
    val monthlyBudget: Long,
    val expenses: List<Expense>,
    val extraIncomes: List<ExtraIncome>,
    val deductions: List<ScheduledDeduction>
)
