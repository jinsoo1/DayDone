package com.jsworld.android.daydone.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.BudgetPeriod
import com.jsworld.android.daydone.domain.model.BudgetProfile
import com.jsworld.android.daydone.domain.model.Expense
import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.domain.model.FutureExpenseStatus
import com.jsworld.android.daydone.domain.usecase.AddFutureExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.CompleteFutureExpensePaymentUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteFutureExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveFutureExpenseStatusesUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveFuturePrepareExpensesUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveHeldPurchasesUseCase
import com.jsworld.android.daydone.domain.usecase.PrepareFutureExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.UndoFutureExpensePaymentUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateFutureExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.WithdrawFuturePreparedUseCase
import com.jsworld.android.daydone.presentation.vault.model.VaultItemUiModel
import com.jsworld.android.daydone.presentation.vault.model.VaultSuggestionUiModel
import com.jsworld.android.daydone.presentation.vault.model.VaultUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val observeFutureExpenseStatusesUseCase: ObserveFutureExpenseStatusesUseCase,
    private val observeFuturePrepareExpensesUseCase: ObserveFuturePrepareExpensesUseCase,
    private val prepareFutureExpenseUseCase: PrepareFutureExpenseUseCase,
    private val completeFutureExpensePaymentUseCase: CompleteFutureExpensePaymentUseCase,
    private val addFutureExpenseUseCase: AddFutureExpenseUseCase,
    private val updateFutureExpenseUseCase: UpdateFutureExpenseUseCase,
    private val deleteFutureExpenseUseCase: DeleteFutureExpenseUseCase,
    private val undoFutureExpensePaymentUseCase: UndoFutureExpensePaymentUseCase,
    private val withdrawFuturePreparedUseCase: WithdrawFuturePreparedUseCase,
    private val observeHeldPurchasesUseCase: ObserveHeldPurchasesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var currentStatuses: List<FutureExpenseStatus> = emptyList()
    private var currentPrepares: List<Expense> = emptyList()
    private var currentSuggestions: List<VaultSuggestionUiModel> = emptyList()
    private var currentPeriod: BudgetPeriod? = null
    private var currentAnchorMonth: YearMonth = YearMonth.now()

    init {
        observeVault()
        observeHeldPurchases()
    }

    private fun observeVault() {
        combine(
            observeBudgetProfileUseCase(),
            observeFutureExpenseStatusesUseCase(),
            observeFuturePrepareExpensesUseCase()
        ) { profile: BudgetProfile, statuses, prepares ->
            Triple(profile, statuses, prepares)
        }.onEach { (profile, statuses, prepares) ->
            val today = LocalDate.now()
            val period = getCurrentBudgetPeriodUseCase(today, profile.budgetStartDay)
            currentPeriod = period
            currentAnchorMonth = YearMonth.from(period.startDate)
            currentStatuses = statuses
            currentPrepares = prepares

            render()
        }.launchIn(viewModelScope)
    }

    /** 보류함 진입 카드 — 아낀 돈(안 삼 + 30일 자동 전환)과 보류 중 건수. */
    private fun observeHeldPurchases() {
        observeHeldPurchasesUseCase()
            .onEach { held ->
                val today = LocalDate.now()
                _uiState.value = _uiState.value.copy(
                    heldSavedTotal = held.filter { it.isSaved(today) }.sumOf { it.amount },
                    heldHoldingCount = held.count { it.isHolding(today) },
                    heldDueBadge = held.any { it.isAutoPassed(today) }
                )
            }
            .launchIn(viewModelScope)
    }

    /** 살까 말까 "금고에 준비하기" — 품목명·가격 프리필, 목표월은 3개월 뒤 기본값. */
    fun onAddItemWithPrefill(title: String, amount: Long) {
        _uiState.value = _uiState.value.copy(
            isInputSheetVisible = true,
            editingId = null,
            titleInput = title,
            categoryInput = FutureExpenseCategory.ETC,
            totalAmountInput = amount.toString(),
            targetMonthInput = currentAnchorMonth.plusMonths(3),
            prepareStartMonthInput = currentAnchorMonth,
            repeatInput = FutureExpenseRepeat.ONCE,
            memoInput = ""
        )
    }

    private fun render() {
        val statuses = currentStatuses
        val period = currentPeriod
        val anchor = currentAnchorMonth
        val prepares = currentPrepares

        val inProgress = statuses.filter { !it.isCompleted }
        val completed = statuses.filter { it.isCompleted }

        val suggestions = inProgress.mapNotNull { status ->
            val item = status.item

            // 준비 창 밖이거나 남은 금액이 없으면 이번 달 제안 없음
            if (anchor < item.prepareStartYearMonth) return@mapNotNull null
            if (anchor > item.targetYearMonth) return@mapNotNull null
            if (status.remainingAmount <= 0L) return@mapNotNull null

            val thisMonthPrepared = if (period != null) {
                prepares.filter {
                    it.futureExpenseId == item.id &&
                        !it.date.isBefore(period.startDate) &&
                        !it.date.isAfter(period.endDate)
                }.sumOf { it.amount }
            } else 0L

            val preparedBefore = (status.preparedAmount - thisMonthPrepared).coerceAtLeast(0L)
            val monthsLeft = (ChronoUnit.MONTHS.between(anchor, item.targetYearMonth).toInt() + 1)
                .coerceAtLeast(1)
            val needed = (item.totalAmount - preparedBefore).coerceAtLeast(0L)
            val monthlyTarget = (needed + monthsLeft - 1) / monthsLeft // ceil
            val remainingThisMonth = (monthlyTarget - thisMonthPrepared).coerceAtLeast(0L)

            VaultSuggestionUiModel(
                id = item.id,
                title = item.title,
                targetMonthLabel = item.targetYearMonth.toLabel(),
                monthlyTarget = monthlyTarget,
                thisMonthPrepared = thisMonthPrepared,
                remainingThisMonth = remainingThisMonth,
                isDone = thisMonthPrepared >= monthlyTarget
            )
        }

        currentSuggestions = suggestions

        _uiState.value = _uiState.value.copy(
            totalPrepared = inProgress.sumOf { it.preparedAmount },
            suggestionTotal = suggestions.sumOf { it.remainingThisMonth },
            suggestions = suggestions,
            items = inProgress.map { it.toUiModel() },
            completedItems = completed.map { it.toUiModel() }
        )
    }

    // --- 준비하기 금액 입력 ---

    fun onPrepareClick(itemId: Long) {
        val suggestion = currentSuggestions.find { it.id == itemId } ?: return
        val prefill = if (suggestion.remainingThisMonth > 0L) {
            suggestion.remainingThisMonth.toString()
        } else {
            ""
        }
        _uiState.value = _uiState.value.copy(
            isPrepareDialogVisible = true,
            prepareTargetId = itemId,
            prepareTargetTitle = suggestion.title,
            prepareMonthlyTarget = suggestion.monthlyTarget,
            prepareAmountInput = prefill
        )
    }

    fun onPrepareAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(prepareAmountInput = value.filter { it.isDigit() })
    }

    fun onPrepareDialogDismiss() {
        _uiState.value = _uiState.value.copy(isPrepareDialogVisible = false, prepareTargetId = null)
    }

    fun onPrepareConfirm() {
        val state = _uiState.value
        val id = state.prepareTargetId ?: return
        val amount = state.prepareAmountInput.toLongOrNull() ?: 0L
        if (amount <= 0L) return

        val status = currentStatuses.find { it.item.id == id } ?: return
        viewModelScope.launch {
            prepareFutureExpenseUseCase(status.item, amount, LocalDate.now())
            _uiState.value = _uiState.value.copy(
                isPrepareDialogVisible = false,
                prepareTargetId = null
            )
        }
    }

    /** 현재 관리(편집) 중인 항목을 납부 완료 처리하고 화면을 닫는다. */
    fun onCompleteEditingPaymentClick() {
        val id = _uiState.value.editingId ?: return
        val status = currentStatuses.find { it.item.id == id } ?: return
        viewModelScope.launch {
            completeFutureExpensePaymentUseCase(status, LocalDate.now())
            _uiState.value = _uiState.value.copy(isInputSheetVisible = false, editingId = null)
        }
    }

    // --- 준비 항목 추가/수정 시트 ---

    fun onAddItemClick() {
        _uiState.value = _uiState.value.copy(
            isInputSheetVisible = true,
            editingId = null,
            titleInput = "",
            categoryInput = FutureExpenseCategory.ETC,
            totalAmountInput = "",
            targetMonthInput = currentAnchorMonth.plusMonths(1),
            prepareStartMonthInput = currentAnchorMonth,
            repeatInput = FutureExpenseRepeat.ONCE,
            memoInput = ""
        )
    }

    fun onItemClick(itemId: Long) {
        val status = currentStatuses.find { it.item.id == itemId } ?: return
        val item = status.item
        _uiState.value = _uiState.value.copy(
            isInputSheetVisible = true,
            editingId = item.id,
            editingPreparedAmount = status.preparedAmount,
            editingIsCompleted = status.isCompleted,
            editingCanUndo = item.lastPaidYearMonth != null,
            titleInput = item.title,
            categoryInput = item.category,
            totalAmountInput = item.totalAmount.toString(),
            targetMonthInput = item.targetYearMonth,
            prepareStartMonthInput = item.prepareStartYearMonth,
            repeatInput = item.repeat,
            memoInput = item.memo.orEmpty()
        )
    }

    fun onUndoPaymentClick() {
        val id = _uiState.value.editingId ?: return
        val item = currentStatuses.find { it.item.id == id }?.item ?: return
        viewModelScope.launch {
            undoFutureExpensePaymentUseCase(item)
            _uiState.value = _uiState.value.copy(isInputSheetVisible = false, editingId = null)
        }
    }

    // --- 준비금 일부 빼기 ---

    fun onWithdrawClick() {
        _uiState.value = _uiState.value.copy(
            isWithdrawDialogVisible = true,
            withdrawMax = _uiState.value.editingPreparedAmount,
            withdrawAmountInput = ""
        )
    }

    fun onWithdrawAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(withdrawAmountInput = value.filter { it.isDigit() })
    }

    fun onWithdrawDismiss() {
        _uiState.value = _uiState.value.copy(isWithdrawDialogVisible = false)
    }

    fun onWithdrawConfirm() {
        val state = _uiState.value
        val id = state.editingId ?: return
        val item = currentStatuses.find { it.item.id == id }?.item ?: return
        val requested = state.withdrawAmountInput.toLongOrNull() ?: 0L
        val amount = requested.coerceAtMost(state.withdrawMax)
        if (amount <= 0L) return

        viewModelScope.launch {
            withdrawFuturePreparedUseCase(
                futureExpenseId = id,
                amount = amount,
                cycleAfter = item.lastPaidYearMonth
            )
            _uiState.value = _uiState.value.copy(
                isWithdrawDialogVisible = false,
                isInputSheetVisible = false,
                editingId = null
            )
        }
    }

    fun onInputDismiss() {
        _uiState.value = _uiState.value.copy(isInputSheetVisible = false, editingId = null)
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(titleInput = value)
    }

    fun onCategoryChange(category: FutureExpenseCategory) {
        _uiState.value = _uiState.value.copy(categoryInput = category)
    }

    fun onTotalAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(totalAmountInput = value.filter { it.isDigit() })
    }

    fun onTargetMonthChange(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(targetMonthInput = yearMonth)
    }

    fun onPrepareStartMonthChange(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(prepareStartMonthInput = yearMonth)
    }

    fun onRepeatChange(repeat: FutureExpenseRepeat) {
        _uiState.value = _uiState.value.copy(repeatInput = repeat)
    }

    fun onMemoChange(value: String) {
        _uiState.value = _uiState.value.copy(memoInput = value)
    }

    fun onSaveItemClick() {
        val state = _uiState.value
        val title = state.titleInput.trim()
        val total = state.totalAmountInput.toLongOrNull() ?: 0L
        val memo = state.memoInput.trim().ifBlank { null }

        if (title.isBlank() || total <= 0L) return

        val editingId = state.editingId

        viewModelScope.launch {
            if (editingId != null) {
                updateFutureExpenseUseCase(
                    id = editingId,
                    title = title,
                    category = state.categoryInput,
                    totalAmount = total,
                    targetYearMonth = state.targetMonthInput,
                    prepareStartYearMonth = state.prepareStartMonthInput,
                    repeat = state.repeatInput,
                    memo = memo
                )
            } else {
                addFutureExpenseUseCase(
                    title = title,
                    category = state.categoryInput,
                    totalAmount = total,
                    targetYearMonth = state.targetMonthInput,
                    prepareStartYearMonth = state.prepareStartMonthInput,
                    repeat = state.repeatInput,
                    memo = memo
                )
            }
            _uiState.value = _uiState.value.copy(isInputSheetVisible = false, editingId = null)
        }
    }

    fun onDeleteItemClick() {
        val editingId = _uiState.value.editingId ?: return
        viewModelScope.launch {
            deleteFutureExpenseUseCase(editingId)
            _uiState.value = _uiState.value.copy(isInputSheetVisible = false, editingId = null)
        }
    }

    private fun FutureExpenseStatus.toUiModel(): VaultItemUiModel {
        val progress = if (item.totalAmount > 0L) {
            (preparedAmount.toFloat() / item.totalAmount.toFloat()).coerceIn(0f, 1f)
        } else 0f

        return VaultItemUiModel(
            id = item.id,
            title = item.title,
            category = item.category,
            totalAmount = item.totalAmount,
            preparedAmount = preparedAmount,
            remainingAmount = remainingAmount,
            targetMonthLabel = item.targetYearMonth.toLabel(),
            progress = progress,
            isCompleted = isCompleted
        )
    }

    private fun YearMonth.toLabel(): String = "${year}년 ${monthValue}월"
}
