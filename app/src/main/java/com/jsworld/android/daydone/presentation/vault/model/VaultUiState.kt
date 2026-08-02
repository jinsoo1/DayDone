package com.jsworld.android.daydone.presentation.vault.model

import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import java.time.YearMonth

data class VaultItemUiModel(
    val id: Long,
    val title: String,
    val category: FutureExpenseCategory,
    val totalAmount: Long,
    val preparedAmount: Long,
    val remainingAmount: Long,
    val targetMonthLabel: String,
    val progress: Float,
    val isCompleted: Boolean
)

data class VaultSuggestionUiModel(
    val id: Long,
    val title: String,
    val targetMonthLabel: String,
    val monthlyTarget: Long,       // 이번 달 추천액(고정)
    val thisMonthPrepared: Long,   // 이번 달 이미 준비한 금액
    val remainingThisMonth: Long,  // 이번 달 남은 추천액
    val isDone: Boolean            // 이번 달 추천만큼 준비 완료
)

data class VaultUiState(
    val totalPrepared: Long = 0L,
    val suggestionTotal: Long = 0L,
    val suggestions: List<VaultSuggestionUiModel> = emptyList(),
    val items: List<VaultItemUiModel> = emptyList(),
    val completedItems: List<VaultItemUiModel> = emptyList(),

    // 준비하기 금액 입력 다이얼로그
    val isPrepareDialogVisible: Boolean = false,
    val prepareTargetId: Long? = null,
    val prepareTargetTitle: String = "",
    val prepareMonthlyTarget: Long = 0L,
    val prepareAmountInput: String = "",

    // 준비금 빼기 다이얼로그
    val isWithdrawDialogVisible: Boolean = false,
    val withdrawMax: Long = 0L,
    val withdrawAmountInput: String = "",

    // 입력 시트 (추가/수정)
    val isInputSheetVisible: Boolean = false,
    val editingId: Long? = null,
    val editingPreparedAmount: Long = 0L,
    val editingIsCompleted: Boolean = false,
    val editingCanUndo: Boolean = false,
    val titleInput: String = "",
    val categoryInput: FutureExpenseCategory = FutureExpenseCategory.ETC,
    val totalAmountInput: String = "",
    val targetMonthInput: YearMonth = YearMonth.now(),
    val prepareStartMonthInput: YearMonth = YearMonth.now(),
    val repeatInput: FutureExpenseRepeat = FutureExpenseRepeat.ONCE,
    val memoInput: String = ""
)
