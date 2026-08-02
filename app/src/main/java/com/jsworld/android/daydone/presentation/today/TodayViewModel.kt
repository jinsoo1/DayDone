package com.jsworld.android.daydone.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.BudgetProfile
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.ExtraIncome
import com.jsworld.android.daydone.domain.model.QuickExpense
import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import com.jsworld.android.daydone.domain.model.ScheduledDeductionAmount
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.usecase.AddExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.AddExtraIncomeUseCase
import com.jsworld.android.daydone.domain.usecase.AddQuickExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.AddScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.usecase.CalculateTodayDefenseLineUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteExtraIncomeUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteQuickExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.usecase.EndScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.usecase.EvaluateNoSpendProgressUseCase
import com.jsworld.android.daydone.domain.usecase.SaveNoSpendChallengeRecordUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateNoSpendChallengeUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.GetScheduledDeductionsInPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.GetTodayDateChipsUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveEffectiveMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.MarkPreJoinSpendHandledUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNoSpendChallengeUseCase
import com.jsworld.android.daydone.domain.usecase.ObservePreJoinSpendHandledUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.ResolveScheduledDeductionAmountsUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExpensesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveExtraIncomesByPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveQuickExpensesUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveScheduledDeductionsUseCase
import com.jsworld.android.daydone.domain.usecase.SetMonthlyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.SetScheduledDeductionAmountUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateExtraIncomeUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateScheduledDeductionUseCase
import com.jsworld.android.daydone.presentation.today.model.QuickExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExtraIncomeUiModel
import com.jsworld.android.daydone.presentation.today.model.ScheduledDeductionSummaryUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayScheduledDeductionUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.math.abs

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val calculateTodayDefenseLineUseCase: CalculateTodayDefenseLineUseCase,
    private val getTodayDateChipsUseCase: GetTodayDateChipsUseCase,
    private val observeExpensesByPeriodUseCase: ObserveExpensesByPeriodUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val updateBudgetProfileUseCase: UpdateBudgetProfileUseCase,
    private val observeEffectiveMonthlyBudgetUseCase: ObserveEffectiveMonthlyBudgetUseCase,
    private val setMonthlyBudgetUseCase: SetMonthlyBudgetUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val addScheduledDeductionUseCase: AddScheduledDeductionUseCase,
    private val updateScheduledDeductionUseCase: UpdateScheduledDeductionUseCase,
    private val deleteScheduledDeductionUseCase: DeleteScheduledDeductionUseCase,
    private val endScheduledDeductionUseCase: EndScheduledDeductionUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase,
    private val setScheduledDeductionAmountUseCase: SetScheduledDeductionAmountUseCase,
    private val observeQuickExpensesUseCase: ObserveQuickExpensesUseCase,
    private val addQuickExpenseUseCase: AddQuickExpenseUseCase,
    private val deleteQuickExpenseUseCase: DeleteQuickExpenseUseCase,
    private val observeExtraIncomesByPeriodUseCase: ObserveExtraIncomesByPeriodUseCase,
    private val addExtraIncomeUseCase: AddExtraIncomeUseCase,
    private val updateExtraIncomeUseCase: UpdateExtraIncomeUseCase,
    private val deleteExtraIncomeUseCase: DeleteExtraIncomeUseCase,
    private val observeNoSpendChallengeUseCase: ObserveNoSpendChallengeUseCase,
    private val evaluateNoSpendProgressUseCase: EvaluateNoSpendProgressUseCase,
    private val updateNoSpendChallengeUseCase: UpdateNoSpendChallengeUseCase,
    private val saveNoSpendChallengeRecordUseCase: SaveNoSpendChallengeRecordUseCase,
    private val observePreJoinSpendHandledUseCase: ObservePreJoinSpendHandledUseCase,
    private val markPreJoinSpendHandledUseCase: MarkPreJoinSpendHandledUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var selectedDate: LocalDate = LocalDate.now()

    /**
     * "오늘"을 흐름으로 들고 있어, 앱을 열어둔 채 자정을 넘겨도
     * 화면 복귀(onResumed) 시 새 날짜 기준으로 다시 계산된다.
     */
    private val todayFlow = MutableStateFlow(LocalDate.now())

    private var currentExpenses: List<Expense> = emptyList()
    private var currentBudgetProfile: BudgetProfile = BudgetProfile(
        monthlyIncome = 3_000_000L,
        payday = 25,
        budgetStartDay = 1
    )

    private var currentAnchorMonth: YearMonth = YearMonth.now()
    private var currentMonthlyBudget: Long = 3_000_000L

    private var currentScheduledDeductions: List<ScheduledDeduction> = emptyList()
    private var currentDeductionOverrides: List<ScheduledDeductionAmount> = emptyList()
    private var currentQuickExpenses: List<QuickExpense> = emptyList()
    private var currentExtraIncomes: List<ExtraIncome> = emptyList()
    private var currentChallengeSettings: NoSpendChallengeSettings? = null
    private var preJoinSpendHandled: Boolean = true // 로딩 전엔 배너 숨김

    init {
        observeTodayData()
        observeChallenge()
    }

    /** 화면 복귀 시 날짜가 바뀌었으면 오늘 기준으로 다시 계산한다. */
    fun onResumed() {
        val now = LocalDate.now()
        if (now == todayFlow.value) return

        // 어제의 '오늘'을 보고 있었다면 새 오늘로 옮겨준다
        if (selectedDate == todayFlow.value) {
            selectedDate = now
        }
        todayFlow.value = now
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeChallenge() {
        combine(
            observeNoSpendChallengeUseCase(),
            todayFlow
        ) { settings, _ -> settings }
            .flatMapLatest { settings ->
                val start = settings.startDate
                if (!settings.enabled || start == null || settings.targetDays <= 0) {
                    kotlinx.coroutines.flow.flowOf(settings to emptyList<Expense>())
                } else {
                    val end = start.plusDays((settings.targetDays - 1).toLong())
                    observeExpensesByPeriodUseCase(startDate = start, endDate = end)
                        .map { settings to it }
                }
            }
            .onEach { (settings, windowExpenses) ->
                val progress = evaluateNoSpendProgressUseCase(
                    expenses = windowExpenses,
                    settings = settings,
                    today = todayFlow.value
                )

                currentChallengeSettings = settings

                // 챌린지 창이 끝났으면 기록으로 남긴다 (시작일 키라 중복 저장 안 됨)
                val start = settings.startDate
                if (settings.enabled && start != null && progress.isFinished) {
                    viewModelScope.launch {
                        saveNoSpendChallengeRecordUseCase(
                            NoSpendChallengeRecord(
                                startDate = start,
                                targetDays = settings.targetDays,
                                successDays = progress.successDays,
                                mode = settings.mode,
                                capAmount = settings.capAmount
                            )
                        )
                    }
                }

                val showEssential = settings.enabled &&
                        (settings.mode == NoSpendMode.ESSENTIAL_ALLOWED ||
                                settings.mode == NoSpendMode.CAP)

                _uiState.value = _uiState.value.copy(
                    challengeEnabled = settings.enabled,
                    challengeMode = settings.mode,
                    challengeCapAmount = settings.capAmount,
                    challengeTargetDays = settings.targetDays,
                    challengeSuccessDays = progress.successDays,
                    challengeTodayOnTrack = progress.isTodayOnTrack,
                    challengeStreak = progress.streak,
                    challengeDayIndex = progress.dayIndex,
                    challengeFinished = progress.isFinished,
                    showEssentialCheckbox = showEssential
                )
            }
            .launchIn(viewModelScope)
    }

    // --- 가입 전 지출 ---

    fun onPreJoinBannerClick() {
        _uiState.value = _uiState.value.copy(
            isPreJoinDialogVisible = true,
            preJoinAmountInput = ""
        )
    }

    fun onPreJoinAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            preJoinAmountInput = value.filter { it.isDigit() }
        )
    }

    /** 다이얼로그만 닫기 — 배너는 유지되어 나중에 다시 열 수 있다. */
    fun onPreJoinDialogDismiss() {
        _uiState.value = _uiState.value.copy(isPreJoinDialogVisible = false)
    }

    /** 입력한 총액을 기간 시작일 지출 1건으로 저장 (오늘 지출로 잡히지 않도록). */
    fun onPreJoinSave() {
        val amount = _uiState.value.preJoinAmountInput.toLongOrNull() ?: 0L
        if (amount <= 0L) return

        val period = getCurrentBudgetPeriodUseCase(
            today = todayFlow.value,
            budgetStartDay = currentBudgetProfile.budgetStartDay
        )

        viewModelScope.launch {
            addExpenseUseCase(
                title = "이전 지출",
                amount = amount,
                date = period.startDate
            )
            markPreJoinSpendHandledUseCase()
            _uiState.value = _uiState.value.copy(isPreJoinDialogVisible = false)
        }
    }

    /** 건너뛰기 — 다시 묻지 않는다. */
    fun onPreJoinSkip() {
        viewModelScope.launch {
            markPreJoinSpendHandledUseCase()
            _uiState.value = _uiState.value.copy(isPreJoinDialogVisible = false)
        }
    }

    /** 완료 카드 닫기: 챌린지를 쉬는 상태로 (설정값은 다음을 위해 유지). */
    fun onChallengeDismissClick() {
        val settings = currentChallengeSettings ?: return
        viewModelScope.launch {
            updateNoSpendChallengeUseCase(
                settings.copy(enabled = false, startDate = null)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayData() {
        combine(
            observeBudgetProfileUseCase(),
            todayFlow
        ) { budgetProfile: BudgetProfile, today: LocalDate -> budgetProfile to today }
            .flatMapLatest { (budgetProfile, today) ->
                val budgetPeriod = getCurrentBudgetPeriodUseCase(
                    today = today,
                    budgetStartDay = budgetProfile.budgetStartDay
                )

                val anchorMonth = YearMonth.from(budgetPeriod.startDate)

                val budgetAndOverrides = combine(
                    observeEffectiveMonthlyBudgetUseCase(
                        anchorMonth = anchorMonth,
                        default = budgetProfile.monthlyIncome
                    ),
                    observeScheduledDeductionAmountsUseCase(),
                    observePreJoinSpendHandledUseCase()
                ) { monthlyBudget, overrides, preJoinHandled ->
                    Triple(monthlyBudget, overrides, preJoinHandled)
                }

                combine(
                    observeExpensesByPeriodUseCase(
                        startDate = budgetPeriod.startDate,
                        endDate = budgetPeriod.endDate
                    ),
                    observeScheduledDeductionsUseCase(),
                    observeQuickExpensesUseCase(),
                    observeExtraIncomesByPeriodUseCase(
                        startDate = budgetPeriod.startDate,
                        endDate = budgetPeriod.endDate
                    ),
                    budgetAndOverrides
                ) { expenses, scheduledDeductions, quickExpenses, extraIncomes, budgetOverrides ->
                    TodayData(
                        budgetProfile = budgetProfile,
                        anchorMonth = anchorMonth,
                        monthlyBudget = budgetOverrides.first,
                        expenses = expenses,
                        scheduledDeductions = scheduledDeductions,
                        deductionOverrides = budgetOverrides.second,
                        preJoinSpendHandled = budgetOverrides.third,
                        quickExpenses = quickExpenses,
                        extraIncomes = extraIncomes
                    )
                }
            }
            .onEach { todayData ->
                currentBudgetProfile = todayData.budgetProfile
                currentAnchorMonth = todayData.anchorMonth
                currentMonthlyBudget = todayData.monthlyBudget
                currentExpenses = todayData.expenses
                currentScheduledDeductions = todayData.scheduledDeductions
                currentDeductionOverrides = todayData.deductionOverrides
                currentQuickExpenses = todayData.quickExpenses
                currentExtraIncomes = todayData.extraIncomes
                preJoinSpendHandled = todayData.preJoinSpendHandled

                loadToday(
                    monthlyIncome = todayData.monthlyBudget,
                    budgetProfile = todayData.budgetProfile,
                    expenses = todayData.expenses,
                    scheduledDeductions = todayData.scheduledDeductions,
                    deductionOverrides = todayData.deductionOverrides,
                    quickExpenses = todayData.quickExpenses,
                    extraIncomes = todayData.extraIncomes
                )
            }
            .launchIn(viewModelScope)
    }

    fun onDateClick(date: LocalDate) {
        selectedDate = date

        loadToday(
            monthlyIncome = currentMonthlyBudget,
            budgetProfile = currentBudgetProfile,
            expenses = currentExpenses,
            scheduledDeductions = currentScheduledDeductions,
            deductionOverrides = currentDeductionOverrides,
            quickExpenses = currentQuickExpenses,
            extraIncomes = currentExtraIncomes
        )
    }

    fun onQuickExpenseClick(item: QuickExpenseUiModel) {
        viewModelScope.launch {
            addExpenseUseCase(
                title = item.title,
                amount = item.amount,
                date = LocalDate.now()
            )

            selectedDate = LocalDate.now()
        }
    }

    fun onExpenseInputClick() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isExpenseInputSheetVisible = true,
            editingExpenseId = null,
            expenseTitleInput = "",
            expenseAmountInput = "",
            expenseDateInput = selectedDate,
            expenseEssentialInput = false
        )
    }

    fun onExpenseRowClick(item: TodayExpenseUiModel) {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isExpenseInputSheetVisible = true,
            editingExpenseId = item.id,
            expenseTitleInput = item.title,
            expenseAmountInput = item.amount.toString(),
            expenseDateInput = item.date,
            expenseEssentialInput = item.isEssential
        )
    }

    fun onExpenseEssentialChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(expenseEssentialInput = value)
    }

    fun onExpenseInputDismiss() {
        _uiState.value = _uiState.value.copy(
            isExpenseInputSheetVisible = false,
            editingExpenseId = null,
            expenseTitleInput = "",
            expenseAmountInput = ""
        )
    }

    fun onExpenseDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            expenseDateInput = date
        )
    }

    fun onExpenseTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(
            expenseTitleInput = value
        )
    }

    fun onExpenseAmountChange(value: String) {
        val onlyDigits = value.filter { it.isDigit() }

        _uiState.value = _uiState.value.copy(
            expenseAmountInput = onlyDigits
        )
    }

    fun onAddExpenseClick() {
        val currentState = _uiState.value

        val title = currentState.expenseTitleInput.trim()
        val amount = currentState.expenseAmountInput.toLongOrNull() ?: 0L
        val date = currentState.expenseDateInput
        val editingId = currentState.editingExpenseId

        if (title.isBlank() || amount <= 0L) {
            return
        }

        viewModelScope.launch {
            if (editingId != null) {
                updateExpenseUseCase(
                    id = editingId,
                    title = title,
                    amount = amount,
                    date = date,
                    isEssential = currentState.expenseEssentialInput
                )
            } else {
                addExpenseUseCase(
                    title = title,
                    amount = amount,
                    date = date,
                    isEssential = currentState.expenseEssentialInput
                )
            }

            selectedDate = date

            _uiState.value = _uiState.value.copy(
                isExpenseInputSheetVisible = false,
                editingExpenseId = null,
                expenseTitleInput = "",
                expenseAmountInput = ""
            )
        }
    }

    fun onDeleteExpenseClick() {
        val editingId = _uiState.value.editingExpenseId ?: return

        viewModelScope.launch {
            deleteExpenseUseCase(editingId)

            _uiState.value = _uiState.value.copy(
                isExpenseInputSheetVisible = false,
                editingExpenseId = null,
                expenseTitleInput = "",
                expenseAmountInput = ""
            )
        }
    }

    fun onBudgetSettingClick() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isBudgetSettingSheetVisible = true,
            monthlyIncomeInput = currentMonthlyBudget.toString(),
            budgetStartDayInput = currentBudgetProfile.budgetStartDay.toString()
        )
    }
    fun onBudgetSettingDismiss() {
        _uiState.value = _uiState.value.copy(
            isBudgetSettingSheetVisible = false
        )
    }

    fun onMonthlyIncomeChange(value: String) {
        val onlyDigits = value.filter { it.isDigit() }

        _uiState.value = _uiState.value.copy(
            monthlyIncomeInput = onlyDigits
        )
    }

    fun onBudgetStartDayChange(value: String) {
        val onlyDigits = value.filter { it.isDigit() }

        _uiState.value = _uiState.value.copy(
            budgetStartDayInput = onlyDigits
        )
    }

    fun onSaveBudgetSettingClick() {
        val currentState = _uiState.value

        val monthlyIncome = currentState.monthlyIncomeInput.toLongOrNull() ?: 0L
        val budgetStartDay = currentState.budgetStartDayInput.toIntOrNull() ?: 1

        if (monthlyIncome <= 0L) {
            return
        }

        if (budgetStartDay !in 1..28) {
            return
        }

        viewModelScope.launch {
            setMonthlyBudgetUseCase(
                anchorMonth = currentAnchorMonth,
                income = monthlyIncome
            )
            updateBudgetProfileUseCase.updateBudgetStartDay(budgetStartDay)

            _uiState.value = _uiState.value.copy(
                isBudgetSettingSheetVisible = false
            )
        }
    }

    fun onScheduledDeductionInputClick() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isScheduledDeductionSheetVisible = true,
            editingScheduledDeductionId = null,
            scheduledDeductionTitleInput = "",
            scheduledDeductionAmountInput = "",
            scheduledDeductionWithdrawalDayInput = "",
            scheduledDeductionTypeInput = ScheduledDeductionType.SAVING
        )
    }

    fun onScheduledDeductionRowClick(id: Long) {
        val deduction = currentScheduledDeductions.find { it.id == id } ?: return

        // 이번 기간(현재 anchor month) 기준 유효 금액을 프리필한다.
        val resolvedAmount = resolveScheduledDeductionAmountsUseCase(
            deductions = listOf(deduction),
            overrides = currentDeductionOverrides,
            anchorMonth = currentAnchorMonth
        ).first().amount

        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isScheduledDeductionSheetVisible = true,
            editingScheduledDeductionId = deduction.id,
            scheduledDeductionTitleInput = deduction.title,
            scheduledDeductionAmountInput = resolvedAmount.toString(),
            scheduledDeductionWithdrawalDayInput = deduction.withdrawalDay.toString(),
            scheduledDeductionTypeInput = deduction.type
        )
    }

    fun onScheduledDeductionInputDismiss() {
        _uiState.value = _uiState.value.copy(
            isScheduledDeductionSheetVisible = false,
            editingScheduledDeductionId = null,
            scheduledDeductionTitleInput = "",
            scheduledDeductionAmountInput = "",
            scheduledDeductionWithdrawalDayInput = ""
        )
    }

    fun onScheduledDeductionTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(
            scheduledDeductionTitleInput = value
        )
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
        _uiState.value = _uiState.value.copy(
            scheduledDeductionTypeInput = type
        )
    }

    fun onSaveScheduledDeductionClick() {
        val currentState = _uiState.value

        val title = currentState.scheduledDeductionTitleInput.trim()
        val amount = currentState.scheduledDeductionAmountInput.toLongOrNull() ?: 0L
        val withdrawalDay = currentState.scheduledDeductionWithdrawalDayInput.toIntOrNull() ?: 0
        val type = currentState.scheduledDeductionTypeInput

        if (title.isBlank()) {
            return
        }

        if (amount <= 0L) {
            return
        }

        if (withdrawalDay !in 1..31) {
            return
        }

        val editingId = currentState.editingScheduledDeductionId

        viewModelScope.launch {
            if (editingId != null) {
                // 금액은 이번 기간부터 이월 적용(월별 오버라이드), 그 외 속성은 항목 전체에 반영.
                val baseAmount = currentScheduledDeductions
                    .find { it.id == editingId }?.amount ?: amount

                updateScheduledDeductionUseCase(
                    id = editingId,
                    title = title,
                    amount = baseAmount,
                    type = type,
                    withdrawalDay = withdrawalDay
                )
                setScheduledDeductionAmountUseCase(
                    deductionId = editingId,
                    anchorMonth = currentAnchorMonth,
                    amount = amount
                )
            } else {
                addScheduledDeductionUseCase(
                    title = title,
                    amount = amount,
                    type = type,
                    withdrawalDay = withdrawalDay,
                    startYearMonth = YearMonth.now(),
                    endYearMonth = null,
                    memo = null
                )
            }

            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null,
                scheduledDeductionTitleInput = "",
                scheduledDeductionAmountInput = "",
                scheduledDeductionWithdrawalDayInput = "",
                scheduledDeductionTypeInput = ScheduledDeductionType.SAVING
            )
        }
    }

    fun onEndScheduledDeductionClick() {
        val editingId = _uiState.value.editingScheduledDeductionId ?: return

        viewModelScope.launch {
            endScheduledDeductionUseCase(
                id = editingId,
                endYearMonth = currentAnchorMonth
            )

            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null,
                scheduledDeductionTitleInput = "",
                scheduledDeductionAmountInput = "",
                scheduledDeductionWithdrawalDayInput = "",
                scheduledDeductionTypeInput = ScheduledDeductionType.SAVING
            )
        }
    }

    fun onDeleteScheduledDeductionClick() {
        val editingId = _uiState.value.editingScheduledDeductionId ?: return

        viewModelScope.launch {
            deleteScheduledDeductionUseCase(editingId)

            _uiState.value = _uiState.value.copy(
                isScheduledDeductionSheetVisible = false,
                editingScheduledDeductionId = null,
                scheduledDeductionTitleInput = "",
                scheduledDeductionAmountInput = "",
                scheduledDeductionWithdrawalDayInput = "",
                scheduledDeductionTypeInput = ScheduledDeductionType.SAVING
            )
        }
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

    fun onFabClick() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = !_uiState.value.isFabMenuExpanded
        )
    }

    fun onFabMenuDismiss() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false
        )
    }

    fun onQuickExpenseAddClick() {
        _uiState.value = _uiState.value.copy(
            isQuickExpenseInputSheetVisible = true,
            quickExpenseTitleInput = "",
            quickExpenseAmountInput = ""
        )
    }

    fun onQuickExpenseInputDismiss() {
        _uiState.value = _uiState.value.copy(
            isQuickExpenseInputSheetVisible = false
        )
    }

    fun onQuickExpenseTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(
            quickExpenseTitleInput = value
        )
    }

    fun onQuickExpenseAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            quickExpenseAmountInput = value.filter { it.isDigit() }
        )
    }

    fun onSaveQuickExpenseClick() {
        val currentState = _uiState.value

        val title = currentState.quickExpenseTitleInput.trim()
        val amount = currentState.quickExpenseAmountInput.toLongOrNull() ?: 0L

        if (title.isBlank()) {
            return
        }

        if (amount <= 0L) {
            return
        }

        viewModelScope.launch {
            addQuickExpenseUseCase(
                title = title,
                amount = amount
            )

            _uiState.value = _uiState.value.copy(
                isQuickExpenseInputSheetVisible = false,
                quickExpenseTitleInput = "",
                quickExpenseAmountInput = ""
            )
        }
    }

    fun onDeleteQuickExpenseClick(id: Long) {
        viewModelScope.launch {
            deleteQuickExpenseUseCase(id)
        }
    }

    fun onExtraIncomeInputClick() {
        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isExtraIncomeInputSheetVisible = true,
            editingExtraIncomeId = null,
            extraIncomeTitleInput = "",
            extraIncomeAmountInput = "",
            extraIncomeMemoInput = "",
            extraIncomeDateInput = selectedDate
        )
    }

    fun onExtraIncomeRowClick(id: Long) {
        val extraIncome = currentExtraIncomes.find { it.id == id } ?: return

        _uiState.value = _uiState.value.copy(
            isFabMenuExpanded = false,
            isExtraIncomeInputSheetVisible = true,
            editingExtraIncomeId = extraIncome.id,
            extraIncomeTitleInput = extraIncome.title,
            extraIncomeAmountInput = extraIncome.amount.toString(),
            extraIncomeMemoInput = extraIncome.memo.orEmpty(),
            extraIncomeDateInput = extraIncome.date
        )
    }

    fun onExtraIncomeInputDismiss() {
        _uiState.value = _uiState.value.copy(
            isExtraIncomeInputSheetVisible = false,
            editingExtraIncomeId = null
        )
    }

    fun onExtraIncomeDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            extraIncomeDateInput = date
        )
    }

    fun onExtraIncomeTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(
            extraIncomeTitleInput = value
        )
    }

    fun onExtraIncomeAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            extraIncomeAmountInput = value.filter { it.isDigit() }
        )
    }

    fun onExtraIncomeMemoChange(value: String) {
        _uiState.value = _uiState.value.copy(
            extraIncomeMemoInput = value
        )
    }

    fun onSaveExtraIncomeClick() {
        val currentState = _uiState.value

        val title = currentState.extraIncomeTitleInput.trim()
        val amount = currentState.extraIncomeAmountInput.toLongOrNull() ?: 0L
        val memo = currentState.extraIncomeMemoInput.trim().ifBlank { null }
        val date = currentState.extraIncomeDateInput
        val editingId = currentState.editingExtraIncomeId

        if (title.isBlank()) {
            return
        }

        if (amount <= 0L) {
            return
        }

        viewModelScope.launch {
            if (editingId != null) {
                updateExtraIncomeUseCase(
                    id = editingId,
                    title = title,
                    amount = amount,
                    date = date,
                    memo = memo
                )
            } else {
                addExtraIncomeUseCase(
                    title = title,
                    amount = amount,
                    date = date,
                    memo = memo
                )
            }

            selectedDate = date

            _uiState.value = _uiState.value.copy(
                isExtraIncomeInputSheetVisible = false,
                editingExtraIncomeId = null,
                extraIncomeTitleInput = "",
                extraIncomeAmountInput = "",
                extraIncomeMemoInput = ""
            )
        }
    }

    fun onDeleteExtraIncomeClick() {
        val editingId = _uiState.value.editingExtraIncomeId ?: return

        viewModelScope.launch {
            deleteExtraIncomeUseCase(editingId)

            _uiState.value = _uiState.value.copy(
                isExtraIncomeInputSheetVisible = false,
                editingExtraIncomeId = null,
                extraIncomeTitleInput = "",
                extraIncomeAmountInput = "",
                extraIncomeMemoInput = ""
            )
        }
    }

    private fun loadToday(
        monthlyIncome: Long,
        budgetProfile: BudgetProfile,
        expenses: List<Expense>,
        scheduledDeductions: List<ScheduledDeduction>,
        deductionOverrides: List<ScheduledDeductionAmount>,
        quickExpenses: List<QuickExpense>,
        extraIncomes: List<ExtraIncome>
    ) {
        val today = todayFlow.value

        val extraIncomeAmount = extraIncomes.sumOf { it.amount }
        val totalAvailableBudget = monthlyIncome + extraIncomeAmount
        val futurePrepareAmount = 0L

        val budgetPeriod = getCurrentBudgetPeriodUseCase(
            today = today,
            budgetStartDay = budgetProfile.budgetStartDay
        )

        val anchorMonth = YearMonth.from(budgetPeriod.startDate)

        val scheduledDeductionsInPeriod =
            resolveScheduledDeductionAmountsUseCase(
                deductions = getScheduledDeductionsInPeriodUseCase(
                    deductions = scheduledDeductions,
                    budgetPeriod = budgetPeriod
                ),
                overrides = deductionOverrides,
                anchorMonth = anchorMonth
            )

        val scheduledSaving = scheduledDeductionsInPeriod
            .filter { it.type == ScheduledDeductionType.SAVING }
            .sumOf { it.amount }

        val fixedExpense = scheduledDeductionsInPeriod
            .filter { it.type == ScheduledDeductionType.FIXED }
            .sumOf { it.amount }

        val scheduledDeductionTotalAmount =
            scheduledSaving + fixedExpense

        val scheduledDeductionSummaries = scheduledDeductionsInPeriod
            .mapNotNull { deduction ->
                val withdrawalDate = resolveWithdrawalDateInPeriod(
                    withdrawalDay = deduction.withdrawalDay,
                    budgetPeriod = budgetPeriod
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

        val selectedDateScheduledDeductions =
            scheduledDeductionSummaries
                .filter { it.withdrawalDate == selectedDate }
                .map { item ->
                    TodayScheduledDeductionUiModel(
                        id = item.id,
                        title = item.title,
                        amount = item.amount,
                        type = item.type,
                        withdrawalDate = item.withdrawalDate
                    )
                }

        val remainingDays = ChronoUnit.DAYS.between(
            today,
            budgetPeriod.endDate
        ).toInt() + 1

        // 새 기간 첫 3일 동안 지난 기간 결산 리포트 안내
        // (단, 지난 기간이 앱을 쓰기 전이면 안내하지 않음 — 신규 유저 첫 기간 보호)
        val dayIndexInPeriod = ChronoUnit.DAYS.between(
            budgetPeriod.startDate,
            today
        ).toInt() + 1
        val firstAnchorMonth = budgetProfile.firstUseDate?.let { firstUse ->
            YearMonth.from(
                getCurrentBudgetPeriodUseCase(
                    today = firstUse,
                    budgetStartDay = budgetProfile.budgetStartDay
                ).startDate
            )
        }
        val previousAnchorMonth = anchorMonth.minusMonths(1)
        val lastPeriodReportMonth = if (
            dayIndexInPeriod <= 3 &&
            firstAnchorMonth != null &&
            !previousAnchorMonth.isBefore(firstAnchorMonth)
        ) {
            previousAnchorMonth.toString()
        } else {
            null
        }

        // 가입 전 지출 배너: 기간 중간에 가입했고, 아직 처리하지 않았고, 가입 후 3일 이내
        // (한참 쓰던 유저에게 뒤늦게 뜨지 않도록 — 그 뒤엔 스스로 월 탭에서 넣을 수 있음)
        val firstUseDate = budgetProfile.firstUseDate
        val showPreJoinBanner = !preJoinSpendHandled &&
                firstUseDate != null &&
                !firstUseDate.isBefore(budgetPeriod.startDate) &&
                !firstUseDate.isAfter(budgetPeriod.endDate) &&
                budgetPeriod.startDate.isBefore(firstUseDate) &&
                ChronoUnit.DAYS.between(firstUseDate, today) <= 3

        val todayExpenseAmount = expenses
            .filter { it.date == today }
            .sumOf { it.amount }

        val pastExpenseAmount = expenses
            .filter { it.date.isBefore(today) }
            .sumOf { it.amount }

        val remainingPureBudgetBeforeTodayExpense =
            totalAvailableBudget -
                    scheduledSaving -
                    fixedExpense -
                    pastExpenseAmount -
                    futurePrepareAmount

        val todayStartDefenseLine = calculateTodayDefenseLineUseCase(
            remainingPureBudget = remainingPureBudgetBeforeTodayExpense,
            today = today,
            budgetPeriod = budgetPeriod
        )

        val todayRemainingDefenseLine =
            todayStartDefenseLine - todayExpenseAmount

        val isTodayOverDefenseLine =
            todayRemainingDefenseLine < 0L

        val todayOverAmount =
            if (isTodayOverDefenseLine) {
                abs(todayRemainingDefenseLine)
            } else {
                0L
            }

        val remainingPureBudget =
            remainingPureBudgetBeforeTodayExpense - todayExpenseAmount

        val expenseDates = expenses
            .map { it.date }
            .toSet()

        val scheduledDeductionDates = scheduledDeductionSummaries
            .map { it.withdrawalDate }
            .toSet()

        val dateChips = getTodayDateChipsUseCase(
            today = today,
            selectedDate = selectedDate,
            expenseDates = expenseDates,
            scheduledDeductionDates = scheduledDeductionDates
        )

        _uiState.value = _uiState.value.copy(
            todayStartDefenseLine = todayStartDefenseLine,
            todayRemainingDefenseLine = todayRemainingDefenseLine,
            todayExpenseAmount = todayExpenseAmount,
            todayOverAmount = todayOverAmount,
            isTodayOverDefenseLine = isTodayOverDefenseLine,

            remainingPureBudget = remainingPureBudget,
            remainingDays = remainingDays,
            budgetPeriodText = "${budgetPeriod.startDate} ~ ${budgetPeriod.endDate}",
            message = if (isTodayOverDefenseLine) {
                "괜찮아요. 남은 날에 다시 나눠볼게요."
            } else {
                "오늘은 이 금액 안에서 쓰면 괜찮아요."
            },

            monthlyIncome = monthlyIncome,
            extraIncomeAmount = extraIncomeAmount,
            totalAvailableBudget = totalAvailableBudget,

            scheduledSavingAmount = scheduledSaving,
            fixedExpenseAmount = fixedExpense,
            scheduledDeductionTotalAmount = scheduledDeductionTotalAmount,

            pastExpenseAmount = pastExpenseAmount,

            dateChips = dateChips,
            selectedDateTitle = selectedDate.toSelectedDateTitle(today),
            lastPeriodReportMonth = lastPeriodReportMonth,
            showPreJoinBanner = showPreJoinBanner,
            periodStartLabel = "${budgetPeriod.startDate.monthValue}월 ${budgetPeriod.startDate.dayOfMonth}일",

            scheduledDeductionSummaries = scheduledDeductionSummaries,
            selectedDateScheduledDeductions = selectedDateScheduledDeductions,

            quickExpenses = quickExpenses.map { quickExpense ->
                QuickExpenseUiModel(
                    id = quickExpense.id,
                    title = quickExpense.title,
                    amount = quickExpense.amount
                )
            },

            selectedDateExpenses = expenses
                .filter { it.date == selectedDate }
                .sortedByDescending { it.id }
                .map {
                    TodayExpenseUiModel(
                        id = it.id,
                        title = it.title,
                        amount = it.amount,
                        date = it.date,
                        isEssential = it.isEssential
                    )
                },

            selectedDateExtraIncomes = extraIncomes
                .filter { it.date == selectedDate }
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
        )
    }

    private fun LocalDate.toSelectedDateTitle(today: LocalDate): String {
        return when {
            this == today -> "오늘 내역"
            this.isBefore(today) -> "${monthValue}월 ${dayOfMonth}일 내역"
            else -> "${monthValue}월 ${dayOfMonth}일 예정"
        }
    }
}

private data class TodayData(
    val budgetProfile: BudgetProfile,
    val anchorMonth: YearMonth,
    val monthlyBudget: Long,
    val expenses: List<Expense>,
    val scheduledDeductions: List<ScheduledDeduction>,
    val deductionOverrides: List<ScheduledDeductionAmount>,
    val preJoinSpendHandled: Boolean,
    val quickExpenses: List<QuickExpense>,
    val extraIncomes: List<ExtraIncome>
)