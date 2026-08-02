package com.jsworld.android.daydone.presentation.monthly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.presentation.monthly.model.MonthViewMode
import com.jsworld.android.daydone.presentation.today.ExpenseInputBottomSheet
import com.jsworld.android.daydone.presentation.today.ExtraIncomeInputBottomSheet
import com.jsworld.android.daydone.presentation.today.ScheduledDeductionInputBottomSheet

@Composable
fun MonthlyRoute(
    onNavigateToReport: (String?) -> Unit = {},
    viewModel: MonthlyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 자정을 넘겨 날짜가 바뀌면 오늘 마커·기간 모드를 다시 계산
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResumed()
    }

    MonthlyScreen(
        uiState = uiState,
        onReportClick = {
            // 진행 중이면 현재 기간, 지난 달이면 그 달의 결산 리포트
            onNavigateToReport(
                if (uiState.mode == MonthViewMode.PAST) uiState.anchorMonthValue else null
            )
        },
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onGoToCurrentMonth = viewModel::onGoToCurrentMonth,
        onDateClick = viewModel::onDateClick,
        onEditBudgetClick = viewModel::onEditBudgetClick,
        onBudgetInputChange = viewModel::onBudgetInputChange,
        onBudgetSheetDismiss = viewModel::onBudgetSheetDismiss,
        onSaveBudgetClick = viewModel::onSaveBudgetClick,
        onExpenseClick = viewModel::onExpenseRowClick,
        onExtraIncomeClick = viewModel::onExtraIncomeRowClick,
        onScheduledDeductionClick = viewModel::onScheduledDeductionRowClick
    )

    if (uiState.isExpenseSheetVisible) {
        ExpenseInputBottomSheet(
            isEditing = true,
            titleInput = uiState.expenseTitleInput,
            amountInput = uiState.expenseAmountInput,
            dateInput = uiState.expenseDateInput,
            isEssentialInput = uiState.expenseEssentialInput,
            showEssential = uiState.showEssentialCheckbox,
            onTitleChange = viewModel::onExpenseTitleChange,
            onAmountChange = viewModel::onExpenseAmountChange,
            onDateChange = viewModel::onExpenseDateChange,
            onEssentialChange = viewModel::onExpenseEssentialChange,
            onDismiss = viewModel::onExpenseSheetDismiss,
            onAddClick = viewModel::onSaveExpenseClick,
            onDeleteClick = viewModel::onDeleteExpenseClick
        )
    }

    if (uiState.isExtraIncomeSheetVisible) {
        ExtraIncomeInputBottomSheet(
            isEditing = true,
            titleInput = uiState.extraIncomeTitleInput,
            amountInput = uiState.extraIncomeAmountInput,
            memoInput = uiState.extraIncomeMemoInput,
            dateInput = uiState.extraIncomeDateInput,
            onTitleChange = viewModel::onExtraIncomeTitleChange,
            onAmountChange = viewModel::onExtraIncomeAmountChange,
            onMemoChange = viewModel::onExtraIncomeMemoChange,
            onDateChange = viewModel::onExtraIncomeDateChange,
            onSaveClick = viewModel::onSaveExtraIncomeClick,
            onDismiss = viewModel::onExtraIncomeSheetDismiss,
            onDeleteClick = viewModel::onDeleteExtraIncomeClick
        )
    }

    if (uiState.isScheduledDeductionSheetVisible) {
        ScheduledDeductionInputBottomSheet(
            isEditing = true,
            titleInput = uiState.scheduledDeductionTitleInput,
            amountInput = uiState.scheduledDeductionAmountInput,
            withdrawalDayInput = uiState.scheduledDeductionWithdrawalDayInput,
            selectedType = uiState.scheduledDeductionTypeInput,
            onTitleChange = viewModel::onScheduledDeductionTitleChange,
            onAmountChange = viewModel::onScheduledDeductionAmountChange,
            onWithdrawalDayChange = viewModel::onScheduledDeductionWithdrawalDayChange,
            onTypeChange = viewModel::onScheduledDeductionTypeChange,
            onDismiss = viewModel::onScheduledDeductionSheetDismiss,
            onSaveClick = viewModel::onSaveScheduledDeductionClick,
            onEndClick = viewModel::onEndScheduledDeductionClick,
            onDeleteClick = viewModel::onDeleteScheduledDeductionClick
        )
    }
}
