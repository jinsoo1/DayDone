package com.jsworld.android.daydone.presentation.vault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VaultRoute(
    onFullScreenChange: (Boolean) -> Unit = {},
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VaultScreen(
        uiState = uiState,
        onFullScreenChange = onFullScreenChange,
        onPrepareClick = viewModel::onPrepareClick,
        onPrepareAmountChange = viewModel::onPrepareAmountChange,
        onPrepareConfirm = viewModel::onPrepareConfirm,
        onPrepareDialogDismiss = viewModel::onPrepareDialogDismiss,
        onCompletePaymentClick = viewModel::onCompleteEditingPaymentClick,
        onUndoPaymentClick = viewModel::onUndoPaymentClick,
        onWithdrawClick = viewModel::onWithdrawClick,
        onWithdrawAmountChange = viewModel::onWithdrawAmountChange,
        onWithdrawConfirm = viewModel::onWithdrawConfirm,
        onWithdrawDismiss = viewModel::onWithdrawDismiss,
        onAddItemClick = viewModel::onAddItemClick,
        onItemClick = viewModel::onItemClick,
        onInputDismiss = viewModel::onInputDismiss,
        onTitleChange = viewModel::onTitleChange,
        onCategoryChange = viewModel::onCategoryChange,
        onTotalAmountChange = viewModel::onTotalAmountChange,
        onTargetMonthChange = viewModel::onTargetMonthChange,
        onPrepareStartMonthChange = viewModel::onPrepareStartMonthChange,
        onRepeatChange = viewModel::onRepeatChange,
        onMemoChange = viewModel::onMemoChange,
        onSaveItemClick = viewModel::onSaveItemClick,
        onDeleteItemClick = viewModel::onDeleteItemClick
    )
}
