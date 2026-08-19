package com.jsworld.android.daydone.presentation.held

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.model.HeldPurchaseStatus
import com.jsworld.android.daydone.domain.model.DailyBudgetSnapshot
import com.jsworld.android.daydone.domain.usecase.AddExpenseUseCase
import com.jsworld.android.daydone.domain.usecase.DeleteHeldPurchaseUseCase
import com.jsworld.android.daydone.domain.usecase.EvaluatePurchaseUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveHeldPurchasesUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveDailyBudgetUseCase
import com.jsworld.android.daydone.domain.usecase.ResolveHeldPurchaseUseCase
import com.jsworld.android.daydone.presentation.today.model.PurchaseEvaluationUiModel
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

/** 30일 창 안에서 보류 중인 항목. */
data class HeldHoldingUiModel(
    val id: Long,
    val title: String,
    val amount: Long,
    val daysHeld: Int,
    val daysLeft: Int,
    val progress: Float
)

/** 30일이 지나 자동으로 아낀 돈이 된 항목 (확인 대기 카드). */
data class HeldDueUiModel(
    val id: Long,
    val title: String,
    val amount: Long
)

/** 확정된 지난 기록. */
data class HeldRecordUiModel(
    val id: Long,
    val title: String,
    val amount: Long,
    val statusLine: String,
    val isSaved: Boolean
)

data class HeldPurchasesUiState(
    val isLoading: Boolean = true,
    val savedTotal: Long = 0L,
    val dueItems: List<HeldDueUiModel> = emptyList(),
    val holdingItems: List<HeldHoldingUiModel> = emptyList(),
    val records: List<HeldRecordUiModel> = emptyList(),

    // 항목 탭 → 오늘 기준 재계산 결과 시트
    val evaluatingId: Long? = null,
    val evaluation: PurchaseEvaluationUiModel? = null,

    // 삭제 확인 다이얼로그
    val deleteConfirmId: Long? = null,
    val deleteConfirmTitle: String = "",
    val deleteConfirmIsSaved: Boolean = false, // 아낀 돈으로 집계 중인 항목인지 (안내 문구용)

    // "지금 살게요" → 지출 프리필 시트
    val isExpenseSheetVisible: Boolean = false,
    val buyingHeldId: Long? = null,
    val expenseTitleInput: String = "",
    val expenseAmountInput: String = "",
    val expenseDateInput: LocalDate = LocalDate.now()
)

@HiltViewModel
class HeldPurchasesViewModel @Inject constructor(
    observeHeldPurchasesUseCase: ObserveHeldPurchasesUseCase,
    observeDailyBudgetUseCase: ObserveDailyBudgetUseCase,
    private val evaluatePurchaseUseCase: EvaluatePurchaseUseCase,
    private val resolveHeldPurchaseUseCase: ResolveHeldPurchaseUseCase,
    private val deleteHeldPurchaseUseCase: DeleteHeldPurchaseUseCase,
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeldPurchasesUiState())
    val uiState: StateFlow<HeldPurchasesUiState> = _uiState.asStateFlow()

    private val today: LocalDate = LocalDate.now()
    private var currentItems: List<HeldPurchase> = emptyList()
    private var currentBudget: DailyBudgetSnapshot? = null

    init {
        combine(
            observeHeldPurchasesUseCase(),
            observeDailyBudgetUseCase(today)
        ) { items, budget -> items to budget }
            .onEach { (items, budget) ->
                currentItems = items
                currentBudget = budget
                render(items)
            }
            .launchIn(viewModelScope)
    }

    private fun render(items: List<HeldPurchase>) {
        val holding = items
            .filter { it.isHolding(today) }
            .map { item ->
                HeldHoldingUiModel(
                    id = item.id,
                    title = item.title,
                    amount = item.amount,
                    daysHeld = item.daysHeld(today),
                    daysLeft = item.daysLeft(today),
                    progress = (item.daysHeld(today).toFloat() / HeldPurchase.HOLD_DAYS)
                        .coerceIn(0f, 1f)
                )
            }

        val due = items
            .filter { it.isAutoPassed(today) }
            .map { HeldDueUiModel(id = it.id, title = it.title, amount = it.amount) }

        val records = items
            .filter { it.status != HeldPurchaseStatus.HELD }
            .sortedByDescending { it.resolvedAt }
            .map { item ->
                val days = item.daysToResolve() ?: 0
                HeldRecordUiModel(
                    id = item.id,
                    title = item.title,
                    amount = item.amount,
                    statusLine = when {
                        item.status == HeldPurchaseStatus.PASSED &&
                                days >= HeldPurchase.HOLD_DAYS ->
                            "30일 동안 안 샀어요 — 아낀 돈이 됐어요"

                        item.status == HeldPurchaseStatus.PASSED ->
                            "${days}일 만에 안 사기로 했어요"

                        else -> "${days}일 고민하고 샀어요"
                    },
                    isSaved = item.status == HeldPurchaseStatus.PASSED
                )
            }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            savedTotal = items.filter { it.isSaved(today) }.sumOf { it.amount },
            dueItems = due,
            holdingItems = holding,
            records = records
        )
    }

    /** 보류 중 항목 탭 — 오늘 기준으로 재계산한 살까 말까 결과. */
    fun onItemClick(id: Long) {
        val item = currentItems.find { it.id == id } ?: return

        val budget = currentBudget ?: return

        val evaluation = evaluatePurchaseUseCase(
            pureBudgetLeft = budget.remainingPureBudget,
            remainingDays = budget.remainingDays,
            price = item.amount
        )

        _uiState.value = _uiState.value.copy(
            evaluatingId = id,
            evaluation = PurchaseEvaluationUiModel(
                title = item.title,
                price = item.amount,
                currentDaily = evaluation.currentDaily,
                afterDaily = evaluation.afterDaily,
                budgetLeft = budget.remainingPureBudget,
                budgetLeftAfter = budget.remainingPureBudget - item.amount,
                remainingDays = budget.remainingDays,
                impact = evaluation.impact
            )
        )
    }

    fun onEvaluationDismiss() {
        _uiState.value = _uiState.value.copy(evaluatingId = null, evaluation = null)
    }

    /** "지금 살게요" — 지출 입력 시트에 프리필, 저장하면 BOUGHT 확정. */
    fun onBuyNowClick(id: Long) {
        val item = currentItems.find { it.id == id } ?: return

        _uiState.value = _uiState.value.copy(
            evaluatingId = null,
            evaluation = null,
            isExpenseSheetVisible = true,
            buyingHeldId = id,
            expenseTitleInput = item.title,
            expenseAmountInput = item.amount.toString(),
            expenseDateInput = today
        )
    }

    /** "안 살래요" — 아낀 돈으로 확정. */
    fun onPassClick(id: Long) {
        viewModelScope.launch {
            resolveHeldPurchaseUseCase(
                id = id,
                status = HeldPurchaseStatus.PASSED,
                resolvedAt = today
            )
            _uiState.value = _uiState.value.copy(evaluatingId = null, evaluation = null)
        }
    }

    /** 30일 도래 카드 "그래도 샀어요" — 아낀 돈에서 제외. 지출은 유저가 직접 입력한다. */
    fun onBoughtAnywayClick(id: Long) {
        viewModelScope.launch {
            resolveHeldPurchaseUseCase(
                id = id,
                status = HeldPurchaseStatus.BOUGHT,
                resolvedAt = today
            )
        }
    }

    /** 30일 도래 카드 확인 — 아낀 돈으로 확정하고 지난 기록으로 내린다. */
    fun onKeepSavedClick(id: Long) {
        val item = currentItems.find { it.id == id } ?: return

        viewModelScope.launch {
            resolveHeldPurchaseUseCase(
                id = id,
                status = HeldPurchaseStatus.PASSED,
                resolvedAt = item.heldAt.plusDays(HeldPurchase.HOLD_DAYS.toLong())
            )
        }
    }

    // --- 삭제 ---

    /** 삭제 확인 다이얼로그 열기 (보류 중 시트·지난 기록 공용). */
    fun onDeleteClick(id: Long) {
        val item = currentItems.find { it.id == id } ?: return

        _uiState.value = _uiState.value.copy(
            deleteConfirmId = id,
            deleteConfirmTitle = item.title,
            deleteConfirmIsSaved = item.isSaved(today)
        )
    }

    fun onDeleteDismiss() {
        _uiState.value = _uiState.value.copy(deleteConfirmId = null)
    }

    fun onDeleteConfirm() {
        val currentState = _uiState.value
        val id = currentState.deleteConfirmId ?: return

        // 재계산 시트가 이 항목을 보고 있었다면 함께 닫는다
        val closingEvaluation = currentState.evaluatingId == id

        viewModelScope.launch {
            deleteHeldPurchaseUseCase(id)
            _uiState.value = _uiState.value.copy(
                deleteConfirmId = null,
                evaluatingId = if (closingEvaluation) null else _uiState.value.evaluatingId,
                evaluation = if (closingEvaluation) null else _uiState.value.evaluation
            )
        }
    }

    // --- 지출 프리필 시트 ---

    fun onExpenseTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseTitleInput = value)
    }

    fun onExpenseAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(
            expenseAmountInput = value.filter { it.isDigit() }
        )
    }

    fun onExpenseDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(expenseDateInput = date)
    }

    fun onExpenseSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isExpenseSheetVisible = false,
            buyingHeldId = null
        )
    }

    fun onExpenseSaveClick() {
        val currentState = _uiState.value
        val title = currentState.expenseTitleInput.trim()
        val amount = currentState.expenseAmountInput.toLongOrNull() ?: 0L
        if (title.isBlank() || amount <= 0L) return

        val heldId = currentState.buyingHeldId

        viewModelScope.launch {
            addExpenseUseCase(
                title = title,
                amount = amount,
                date = currentState.expenseDateInput
            )
            if (heldId != null) {
                resolveHeldPurchaseUseCase(
                    id = heldId,
                    status = HeldPurchaseStatus.BOUGHT,
                    resolvedAt = today
                )
            }
            _uiState.value = _uiState.value.copy(
                isExpenseSheetVisible = false,
                buyingHeldId = null
            )
        }
    }
}
