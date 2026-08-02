package com.jsworld.android.daydone.presentation.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.BudgetProfile
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.usecase.DeleteExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteExtraIncomeUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.usecase.EndScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.usecase.EvaluateNoSpendProgressUseCase
import com.jsworld.android.daydone.domain.usecase.GetBudgetPeriodForMonthUseCase
import com.jsworld.android.daydone.domain.usecase.GetScheduledDeductionsInPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveEffectiveMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNoSpendChallengeUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExpensesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExtraIncomesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionsUseCase
import com.jsworld.android.daydone.domain.usecase.ResolveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.SetMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.SetScheduledDeductionAmountUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateExtraIncomeUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateScheduledDeductionUseCase
import com.jsworld.android.daydone.presentation.monthly.model.MonthViewMode
import com.jsworld.android.daydone.presentation.monthly.model.MonthlyDayCellUiModel
import com.jsworld.android.daydone.presentation.monthly.model.MonthlyUiState
import com.jsworld.android.daydone.presentation.today.model.ScheduledDeductionSummaryUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExtraIncomeUiModel
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

@HiltViewModel
class MonthlyViewModel @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getBudgetPeriodForMonthUseCase: GetBudgetPeriodForMonthUseCase,
    private val getCurrentBudgetPeriodUseCase: com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase,
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val observeExtraIncomesByPeriodUseCase: ObserveExtraIncomesByPeriodUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val observeEffectiveMonthlyBudgetUseCase: ObserveEffectiveMonthlyBudgetUseCase,
    private val setMonthlyBudgetUseCase: SetMonthlyBudgetUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val updateExtraIncomeUseCase: UpdateExtraIncomeUseCase,
    private val deleteExtraIncomeUseCase: DeleteExtraIncomeUseCase,
    private val updateScheduledDeductionUseCase: UpdateScheduledDeductionUseCase,
    private val deleteScheduledDeductionUseCase: DeleteScheduledDeductionUseCase,
    private val endScheduledDeductionUseCase: EndScheduledDeductionUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase,
    private val setScheduledDeductionAmountUseCase: SetScheduledDeductionAmountUseCase,
    private val observeNoSpendChallengeUseCase: ObserveNoSpendChallengeUseCase,
    private val evaluateNoSpendProgressUseCase: EvaluateNoSpendProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyUiState())
    val uiState: StateFlow<MonthlyUiState> = _uiState.asStateFlow()

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private var currentData: MonthlyData? = null
    private var minAnchorMonth: YearMonth? = null // 앱 시작 달 (이전 탐색 하한)
    private var currentBudget: Long = 0L
    private var selectedDate: LocalDate? = null
    private var currentChallenge: NoSpendChallengeSettings? = null

    init {
        observeMonthlyData()
        observeChallenge()
    }

    private fun observeChallenge() {
        observeNoSpendChallengeUseCase()
            .onEach { settings ->
                currentChallenge = settings
                val show = settings.enabled &&
                        (settings.mode == NoSpendMode.ESSENTIAL_ALLOWED ||
                                settings.mode == NoSpendMode.CAP)
                _uiState.value = _uiState.value.copy(showEssentialCheckbox = show)
                if (currentData != null) render()
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMonthlyData() {
        combine(
            observeBudgetProfileUseCase(),
            selectedMonth
        ) { profile, yearMonth ->
            profile to yearMonth
        }.flatMapLatest { (profile, yearMonth) ->
            val period = getBudgetPeriodForMonthUseCase(
                yearMonth = yearMonth,
                budgetStartDay = profile.budgetStartDay
            )

            // 앱을 쓰기 시작한 날이 속한 기간의 달 — 그 이전 달은 탐색/리포트 하한.
            // firstUseDate가 없으면(온보딩 전 기존 설치) 제한하지 않는다.
            minAnchorMonth = profile.firstUseDate?.let { firstUse ->
                YearMonth.from(
                    getCurrentBudgetPeriodUseCase(
                        today = firstUse,
                        budgetStartDay = profile.budgetStartDay
                    ).startDate
                )
            }

            combine(
                observeExpensesByPeriodUseCase(
                    startDate = period.startDate,
                    endDate = period.endDate
                ),
                observeScheduledDeductionsUseCase(),
                observeExtraIncomesByPeriodUseCase(
                    startDate = period.startDate,
                    endDate = period.endDate
                ),
                observeEffectiveMonthlyBudgetUseCase(
                    anchorMonth = yearMonth,
                    default = profile.monthlyIncome
                ),
                observeScheduledDeductionAmountsUseCase()
            ) { expenses, scheduledDeductions, extraIncomes, monthlyBudget, deductionOverrides ->
                MonthlyData(
                    profile = profile,
                    yearMonth = yearMonth,
                    period = period,
                    expenses = expenses,
                    scheduledDeductions = scheduledDeductions,
                    extraIncomes = extraIncomes,
                    monthlyBudget = monthlyBudget,
                    deductionOverrides = deductionOverrides
                )
            }
        }.onEach { data ->
            currentData = data
            render()
        }.launchIn(viewModelScope)
    }

    private fun render() {
        val data = currentData ?: return

        val today = LocalDate.now()
        val yearMonth = data.yearMonth
        val period = data.period

        currentBudget = data.monthlyBudget

        val deductionsInPeriod = resolveScheduledDeductionAmountsUseCase(
            deductions = getScheduledDeductionsInPeriodUseCase(
                deductions = data.scheduledDeductions,
                budgetPeriod = period
            ),
            overrides = data.deductionOverrides,
            anchorMonth = yearMonth
        )

        val scheduledSaving = deductionsInPeriod
            .filter { it.type == ScheduledDeductionType.SAVING }
            .sumOf { it.amount }

        val fixedExpense = deductionsInPeriod
            .filter { it.type == ScheduledDeductionType.FIXED }
            .sumOf { it.amount }

        val scheduledDeductionTotal = scheduledSaving + fixedExpense

        val extraIncomeAmount = data.extraIncomes.sumOf { it.amount }
        val totalAvailableBudget = data.monthlyBudget + extraIncomeAmount
        val totalExpense = data.expenses.sumOf { it.amount }
        val remainingAmount =
            totalAvailableBudget - scheduledDeductionTotal - totalExpense

        val mode = when {
            today.isAfter(period.endDate) -> MonthViewMode.PAST
            today.isBefore(period.startDate) -> MonthViewMode.FUTURE
            else -> MonthViewMode.CURRENT
        }

        val summaries = deductionsInPeriod
            .mapNotNull { deduction ->
                val withdrawalDate = resolveWithdrawalDateInPeriod(
                    withdrawalDay = deduction.withdrawalDay,
                    budgetPeriod = period
                ) ?: return@mapNotNull null

                ScheduledDeductionSummaryUiModel(
                    id = deduction.id,
                    title = deduction.title,
                    amount = deduction.amount,
                    type = deduction.type,
                    withdrawalDate = withdrawalDate
                )
            }
            .sortedBy { it.withdrawalDate }

        // 선택 날짜 확정: 기존 선택이 이 기간 안이면 유지, 아니면 오늘(기간 내) 또는 시작일.
        val effectiveSelected = selectedDate
            ?.takeIf { !it.isBefore(period.startDate) && !it.isAfter(period.endDate) }
            ?: if (!today.isBefore(period.startDate) && !today.isAfter(period.endDate)) {
                today
            } else {
                period.startDate
            }
        selectedDate = effectiveSelected

        val expenseDates = data.expenses.map { it.date }.toSet()
        val scheduledDeductionDates = summaries.map { it.withdrawalDate }.toSet()
        val noSpendSuccessDates = computeNoSpendSuccessDates(
            challenge = currentChallenge,
            period = period,
            today = today,
            expenses = data.expenses
        )

        val calendarWeeks = buildCalendarWeeks(
            period = period,
            today = today,
            selectedDate = effectiveSelected,
            expenseDates = expenseDates,
            scheduledDeductionDates = scheduledDeductionDates,
            noSpendSuccessDates = noSpendSuccessDates
        )

        val selectedExpenses = data.expenses
            .filter { it.date == effectiveSelected }
            .sortedByDescending { it.id }
            .map {
                TodayExpenseUiModel(
                    id = it.id,
                    title = it.title,
                    amount = it.amount,
                    date = it.date
                )
            }

        val selectedExtraIncomes = data.extraIncomes
            .filter { it.date == effectiveSelected }
            .sortedByDescending { it.id }
            .map {
                TodayExtraIncomeUiModel(
                    id = it.id,
                    title = it.title,
                    amount = it.amount,
                    date = it.date,
                    memo = it.memo
                )
            }

        val selectedScheduledDeductions = summaries
            .filter { it.withdrawalDate == effectiveSelected }

        _uiState.value = _uiState.value.copy(
            monthTitle = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            anchorMonthValue = yearMonth.toString(),
            canGoPrevious = minAnchorMonth?.let { yearMonth.isAfter(it) } ?: true,
            periodText = "${period.startDate} ~ ${period.endDate}",
            mode = mode,
            monthlyBudget = data.monthlyBudget,
            extraIncomeAmount = extraIncomeAmount,
            totalAvailableBudget = totalAvailableBudget,
            scheduledSavingAmount = scheduledSaving,
            fixedExpenseAmount = fixedExpense,
            scheduledDeductionTotalAmount = scheduledDeductionTotal,
            totalExpense = totalExpense,
            remainingAmount = remainingAmount,
            scheduledDeductionSummaries = summaries,
            calendarWeeks = calendarWeeks,
            selectedDateTitle = selectedDateTitle(effectiveSelected, today),
            selectedDateExpenses = selectedExpenses,
            selectedDateExtraIncomes = selectedExtraIncomes,
            selectedDateScheduledDeductions = selectedScheduledDeductions
        )
    }

    fun onPreviousMonth() {
        // 앱 시작 달 이전으로는 이동하지 않음 (데이터 없는 가짜 기간 방지)
        val min = minAnchorMonth
        if (min != null && !selectedMonth.value.isAfter(min)) return

        selectedDate = null
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        selectedDate = null
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    /** 화면 복귀 시 날짜가 바뀌었으면 오늘 마커·모드를 다시 계산한다. */
    fun onResumed() {
        if (currentData != null) render()
    }

    /** 지난/다가올 달을 보다가 이번 달로 바로 복귀. */
    fun onGoToCurrentMonth() {
        selectedDate = null
        selectedMonth.value = YearMonth.now()
    }

    fun onDateClick(date: LocalDate) {
        selectedDate = date
        render()
    }

    fun onEditBudgetClick() {
        _uiState.value = _uiState.value.copy(
            isBudgetSheetVisible = true,
            budgetInput = currentBudget.toString()
        )
    }

    fun onBudgetInputChange(value: String) {
        _uiState.value = _uiState.value.copy(
            budgetInput = value.filter { it.isDigit() }
        )
    }

    fun onBudgetSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isBudgetSheetVisible = false
        )
    }

    fun onSaveBudgetClick() {
        val amount = _uiState.value.budgetInput.toLongOrNull() ?: 0L

        if (amount <= 0L) {
            return
        }

        viewModelScope.launch {
            setMonthlyBudgetUseCase(
                anchorMonth = selectedMonth.value,
                income = amount
            )

            _uiState.value = _uiState.value.copy(
                isBudgetSheetVisible = false
            )
        }
    }

    // --- 지출 수정 / 삭제 ---

    fun onExpenseRowClick(id: Long) {
        val expense = currentData?.expenses?.find { it.id == id } ?: return
        _uiState.value = _uiState.value.copy(
            isExpenseSheetVisible = true,
            editingExpenseId = expense.id,
            expenseTitleInput = expense.title,
            expenseAmountInput = expense.amount.toString(),
            expenseDateInput = expense.date,
            expenseEssentialInput = expense.isEssential
        )
    }

    fun onExpenseEssentialChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(expenseEssentialInput = value)
    }

    fun onExpenseTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseTitleInput = value)
    }

    fun onExpenseAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseAmountInput = value.filter { it.isDigit() })
    }

    fun onExpenseDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(expenseDateInput = date)
    }

    fun onExpenseSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isExpenseSheetVisible = false,
            editingExpenseId = null
        )
    }

    fun onSaveExpenseClick() {
        val state = _uiState.value
        val id = state.editingExpenseId ?: return
        val title = state.expenseTitleInput.trim()
        val amount = state.expenseAmountInput.toLongOrNull() ?: 0L

        if (title.isBlank() || amount <= 0L) {
            return
        }

        viewModelScope.launch {
            updateExpenseUseCase(
                id = id,
                title = title,
                amount = amount,
                date = state.expenseDateInput,
                isEssential = state.expenseEssentialInput
            )
            _uiState.value = _uiState.value.copy(
                isExpenseSheetVisible = false,
                editingExpenseId = null
            )
        }
    }

    fun onDeleteExpenseClick() {
        val id = _uiState.value.editingExpenseId ?: return
        viewModelScope.launch {
            deleteExpenseUseCase(id)
            _uiState.value = _uiState.value.copy(
                isExpenseSheetVisible = false,
                editingExpenseId = null
            )
        }
    }

    // --- 추가 수익 수정 / 삭제 ---

    fun onExtraIncomeRowClick(id: Long) {
        val income = currentData?.extraIncomes?.find { it.id == id } ?: return
        _uiState.value = _uiState.value.copy(
            isExtraIncomeSheetVisible = true,
            editingExtraIncomeId = income.id,
            extraIncomeTitleInput = income.title,
            extraIncomeAmountInput = income.amount.toString(),
            extraIncomeMemoInput = income.memo.orEmpty(),
            extraIncomeDateInput = income.date
        )
    }

    fun onExtraIncomeTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(extraIncomeTitleInput = value)
    }

    fun onExtraIncomeAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(extraIncomeAmountInput = value.filter { it.isDigit() })
    }

    fun onExtraIncomeMemoChange(value: String) {
        _uiState.value = _uiState.value.copy(extraIncomeMemoInput = value)
    }

    fun onExtraIncomeDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(extraIncomeDateInput = date)
    }

    fun onExtraIncomeSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isExtraIncomeSheetVisible = false,
            editingExtraIncomeId = null
        )
    }

    fun onSaveExtraIncomeClick() {
        val state = _uiState.value
        val id = state.editingExtraIncomeId ?: return
        val title = state.extraIncomeTitleInput.trim()
        val amount = state.extraIncomeAmountInput.toLongOrNull() ?: 0L
        val memo = state.extraIncomeMemoInput.trim().ifBlank { null }

        if (title.isBlank() || amount <= 0L) {
            return
        }

        viewModelScope.launch {
            updateExtraIncomeUseCase(
                id = id,
                title = title,
                amount = amount,
                date = state.extraIncomeDateInput,
                memo = memo
            )
            _uiState.value = _uiState.value.copy(
                isExtraIncomeSheetVisible = false,
                editingExtraIncomeId = null
            )
        }
    }

    fun onDeleteExtraIncomeClick() {
        val id = _uiState.value.editingExtraIncomeId ?: return
        viewModelScope.launch {
            deleteExtraIncomeUseCase(id)
            _uiState.value = _uiState.value.copy(
                isExtraIncomeSheetVisible = false,
                editingExtraIncomeId = null
            )
        }
    }

    // --- 저축 / 고정비 수정 / 삭제 ---

    fun onScheduledDeductionRowClick(id: Long) {
        val deduction = currentData?.scheduledDeductions?.find { it.id == id } ?: return

        // 보고 있는 달 기준 유효 금액을 프리필.
        val resolvedAmount = resolveScheduledDeductionAmountsUseCase(
            deductions = listOf(deduction),
            overrides = currentData?.deductionOverrides ?: emptyList(),
            anchorMonth = selectedMonth.value
        ).first().amount

        _uiState.value = _uiState.value.copy(
            isScheduledDeductionSheetVisible = true,
            editingScheduledDeductionId = deduction.id,
            scheduledDeductionTitleInput = deduction.title,
            scheduledDeductionAmountInput = resolvedAmount.toString(),
            scheduledDeductionWithdrawalDayInput = deduction.withdrawalDay.toString(),
            scheduledDeductionTypeInput = deduction.type
        )
    }

    fun onScheduledDeductionTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(scheduledDeductionTitleInput = value)
    }

    fun onScheduledDeductionAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            scheduledDeductionAmountInput = value.filter { it.isDigit() }
        )
    }

    fun onScheduledDeductionWithdrawalDayChange(value: String) {
        _uiState.value = _uiState.value.copy(
            scheduledDeductionWithdrawalDayInput = value.filter { it.isDigit() }
        )
    }

    fun onScheduledDeductionTypeChange(type: ScheduledDeductionType) {
        _uiState.value = _uiState.value.copy(scheduledDeductionTypeInput = type)
    }

    fun onScheduledDeductionSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isScheduledDeductionSheetVisible = false,
            editingScheduledDeductionId = null
        )
    }

    fun onSaveScheduledDeductionClick() {
        val state = _uiState.value
        val id = state.editingScheduledDeductionId ?: return
        val title = state.scheduledDeductionTitleInput.trim()
        val amount = state.scheduledDeductionAmountInput.toLongOrNull() ?: 0L
        val withdrawalDay = state.scheduledDeductionWithdrawalDayInput.toIntOrNull() ?: 0

        if (title.isBlank() || amount <= 0L || withdrawalDay !in 1..31) {
            return
        }

        viewModelScope.launch {
            // 금액은 보고 있는 달부터 이월 적용(월별 오버라이드), 그 외 속성은 항목 전체에 반영.
            val baseAmount = currentData?.scheduledDeductions
                ?.find { it.id == id }?.amount ?: amount

            updateScheduledDeductionUseCase(
                id = id,
                title = title,
                amount = baseAmount,
                type = state.scheduledDeductionTypeInput,
                withdrawalDay = withdrawalDay
            )
            setScheduledDeductionAmountUseCase(
                deductionId = id,
                anchorMonth = selectedMonth.value,
                amount = amount
            )
            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null
            )
        }
    }

    fun onEndScheduledDeductionClick() {
        val id = _uiState.value.editingScheduledDeductionId ?: return
        viewModelScope.launch {
            endScheduledDeductionUseCase(
                id = id,
                endYearMonth = selectedMonth.value
            )
            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null
            )
        }
    }

    fun onDeleteScheduledDeductionClick() {
        val id = _uiState.value.editingScheduledDeductionId ?: return
        viewModelScope.launch {
            deleteScheduledDeductionUseCase(id)
            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null
            )
        }
    }

    private fun selectedDateTitle(
        selectedDate: LocalDate,
        today: LocalDate
    ): String {
        return when {
            selectedDate == today -> "오늘 내역"
            selectedDate.isBefore(today) ->
                "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 내역"
            else ->
                "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 예정"
        }
    }

    private fun buildCalendarWeeks(
        period: BudgetPeriod,
        today: LocalDate,
        selectedDate: LocalDate,
        expenseDates: Set<LocalDate>,
        scheduledDeductionDates: Set<LocalDate>,
        noSpendSuccessDates: Set<LocalDate>
    ): List<List<MonthlyDayCellUiModel?>> {
        val cells = mutableListOf<MonthlyDayCellUiModel?>()

        // 일요일=0 기준 앞쪽 빈 칸. dayOfWeek: 월=1 … 일=7 → % 7 로 일=0.
        val leadingBlanks = period.startDate.dayOfWeek.value % 7
        repeat(leadingBlanks) { cells.add(null) }

        var cursor = period.startDate
        while (!cursor.isAfter(period.endDate)) {
            cells.add(
                MonthlyDayCellUiModel(
                    date = cursor,
                    dayText = cursor.dayOfMonth.toString(),
                    isToday = cursor == today,
                    isSelected = cursor == selectedDate,
                    hasExpense = expenseDates.contains(cursor),
                    hasScheduledDeduction = scheduledDeductionDates.contains(cursor),
                    isNoSpendSuccess = noSpendSuccessDates.contains(cursor)
                )
            )
            cursor = cursor.plusDays(1)
        }

        while (cells.size % 7 != 0) {
            cells.add(null)
        }

        return cells.chunked(7)
    }

    /** 무지출 챌린지가 진행/완료된 구간에서, 이 달 안의 "확정 성공한 지난 날" 집합. */
    private fun computeNoSpendSuccessDates(
        challenge: NoSpendChallengeSettings?,
        period: BudgetPeriod,
        today: LocalDate,
        expenses: List<Expense>
    ): Set<LocalDate> {
        val start = challenge?.startDate
        if (challenge == null || !challenge.enabled || start == null || challenge.targetDays <= 0) {
            return emptySet()
        }

        val windowEnd = start.plusDays((challenge.targetDays - 1).toLong())

        // 이 달 기간 ∩ 챌린지 창 ∩ (오늘 이전: 확정된 날만)
        val from = maxOf(start, period.startDate)
        val to = minOf(windowEnd, period.endDate, today.minusDays(1))
        if (from.isAfter(to)) return emptySet()

        val byDate = expenses.groupBy { it.date }
        val result = mutableSetOf<LocalDate>()
        var cursor = from
        while (!cursor.isAfter(to)) {
            if (evaluateNoSpendProgressUseCase.isSuccessDay(byDate[cursor].orEmpty(), challenge)) {
                result.add(cursor)
            }
            cursor = cursor.plusDays(1)
        }
        return result
    }

    private fun resolveWithdrawalDateInPeriod(
        withdrawalDay: Int,
        budgetPeriod: BudgetPeriod
    ): LocalDate? {
        var cursor = budgetPeriod.startDate

        while (!cursor.isAfter(budgetPeriod.endDate)) {
            val correctedDay = minOf(
                withdrawalDay,
                cursor.lengthOfMonth()
            )

            val candidate = cursor.withDayOfMonth(correctedDay)

            if (
                !candidate.isBefore(budgetPeriod.startDate) &&
                !candidate.isAfter(budgetPeriod.endDate)
            ) {
                return candidate
            }

            cursor = cursor.plusMonths(1).withDayOfMonth(1)
        }

        return null
    }
}

private data class MonthlyData(
    val profile: BudgetProfile,
    val yearMonth: YearMonth,
    val period: BudgetPeriod,
    val expenses: List<Expense>,
    val scheduledDeductions: List<ScheduledDeduction>,
    val extraIncomes: List<ExtraIncome>,
    val monthlyBudget: Long,
    val deductionOverrides: List<ScheduledDeductionAmount>
)
