package com.jsworld.android.daydone.presentation.vault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.presentation.navigation.VaultAddPrefill

@Composable
fun VaultRoute(
    onFullScreenChange: (Boolean) -> Unit = {},
    pendingPrefill: VaultAddPrefill? = null,
    onPendingPrefillConsumed: () -> Unit = {},
    onNavigateToHeldPurchases: () -> Unit = {},
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 살까 말까 "금고에 준비하기" → 추가 시트 프리필로 열기
    LaunchedEffect(pendingPrefill) {
        if (pendingPrefill != null) {
            viewModel.onAddItemWithPrefill(
                title = pendingPrefill.title,
                amount = pendingPrefill.amount
            )
            onPendingPrefillConsumed()
        }
    }

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
        onDeleteItemClick = viewModel::onDeleteItemClick,
        onHeldPurchasesClick = onNavigateToHeldPurchases
    )
}
