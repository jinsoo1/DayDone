package com.jsworld.android.daydone.presentation.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.presentation.challenge.ChallengeSettingsSheet
import com.jsworld.android.daydone.presentation.navigation.AddType
import com.jsworld.android.daydone.ui.component.DayDoneTopBar

@Composable
fun TodayRoute(
    pendingAdd: AddType?,
    onPendingAddConsumed: () -> Unit,
    onOpenReport: (String) -> Unit = {},
    onPrepareInVault: (title: String, amount: Long) -> Unit = { _, _ -> },
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 앱을 열어둔 채 자정을 넘겼을 때 오늘 기준으로 다시 계산
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResumed()
    }

    // 완료 카드 "새로 도전하기" → 오늘 탭 위에서 바로 챌린지 설정 시트
    var showChallengeSheet by rememberSaveable { mutableStateOf(false) }

    // 하단 중앙 '+'에서 넘어온 빠른 입력 요청을 해당 입력 시트로 연결.
    LaunchedEffect(pendingAdd) {
        when (pendingAdd) {
            AddType.EXPENSE -> viewModel.onExpenseInputClick()
            AddType.INCOME -> viewModel.onExtraIncomeInputClick()
            AddType.DEDUCTION -> viewModel.onScheduledDeductionInputClick()
            AddType.BUDGET -> viewModel.onBudgetSettingClick()
            AddType.PURCHASE -> viewModel.onPurchaseDecisionClick()
            null -> {}
        }
        if (pendingAdd != null) {
            onPendingAddConsumed()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DayDoneTopBar(title = "데이던")

        TodayScreen(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            onDateClick = viewModel::onDateClick,
            onQuickExpenseClick = viewModel::onQuickExpenseClick,

            onExpenseRowClick = viewModel::onExpenseRowClick,
            onExpenseInputDismiss = viewModel::onExpenseInputDismiss,
            onExpenseTitleChange = viewModel::onExpenseTitleChange,
            onExpenseAmountChange = viewModel::onExpenseAmountChange,
            onExpenseDateChange = viewModel::onExpenseDateChange,
            onExpenseEssentialChange = viewModel::onExpenseEssentialChange,
            onChallengeRestartClick = { showChallengeSheet = true },
            onChallengeDismissClick = viewModel::onChallengeDismissClick,
            onLastReportClick = { uiState.lastPeriodReportMonth?.let(onOpenReport) },
            onPreJoinBannerClick = viewModel::onPreJoinBannerClick,
            onPreJoinAmountChange = viewModel::onPreJoinAmountChange,
            onPreJoinDialogDismiss = viewModel::onPreJoinDialogDismiss,
            onPreJoinSave = viewModel::onPreJoinSave,
            onPreJoinSkip = viewModel::onPreJoinSkip,
            onAddExpenseClick = viewModel::onAddExpenseClick,
            onDeleteExpenseClick = viewModel::onDeleteExpenseClick,

            onBudgetSettingDismiss = viewModel::onBudgetSettingDismiss,
            onMonthlyIncomeChange = viewModel::onMonthlyIncomeChange,
            onBudgetStartDayChange = viewModel::onBudgetStartDayChange,
            onSaveBudgetSettingClick = viewModel::onSaveBudgetSettingClick,

            onScheduledDeductionRowClick = viewModel::onScheduledDeductionRowClick,
            onScheduledDeductionInputDismiss = viewModel::onScheduledDeductionInputDismiss,
            onScheduledDeductionTitleChange = viewModel::onScheduledDeductionTitleChange,
            onScheduledDeductionAmountChange = viewModel::onScheduledDeductionAmountChange,
            onScheduledDeductionWithdrawalDayChange = viewModel::onScheduledDeductionWithdrawalDayChange,
            onScheduledDeductionTypeChange = viewModel::onScheduledDeductionTypeChange,
            onSaveScheduledDeductionClick = viewModel::onSaveScheduledDeductionClick,
            onEndScheduledDeductionClick = viewModel::onEndScheduledDeductionClick,
            onDeleteScheduledDeductionClick = viewModel::onDeleteScheduledDeductionClick,

            onQuickExpenseAddClick = viewModel::onQuickExpenseAddClick,
            onQuickExpenseInputDismiss = viewModel::onQuickExpenseInputDismiss,
            onQuickExpenseTitleChange = viewModel::onQuickExpenseTitleChange,
            onQuickExpenseAmountChange = viewModel::onQuickExpenseAmountChange,
            onSaveQuickExpenseClick = viewModel::onSaveQuickExpenseClick,
            onDeleteQuickExpenseClick = viewModel::onDeleteQuickExpenseClick,

            onExtraIncomeRowClick = viewModel::onExtraIncomeRowClick,
            onExtraIncomeInputDismiss = viewModel::onExtraIncomeInputDismiss,
            onExtraIncomeTitleChange = viewModel::onExtraIncomeTitleChange,
            onExtraIncomeAmountChange = viewModel::onExtraIncomeAmountChange,
            onExtraIncomeMemoChange = viewModel::onExtraIncomeMemoChange,
            onExtraIncomeDateChange = viewModel::onExtraIncomeDateChange,
            onSaveExtraIncomeClick = viewModel::onSaveExtraIncomeClick,
            onDeleteExtraIncomeClick = viewModel::onDeleteExtraIncomeClick,

            onPurchaseSheetDismiss = viewModel::onPurchaseSheetDismiss,
            onPurchaseTitleChange = viewModel::onPurchaseTitleChange,
            onPurchaseAmountChange = viewModel::onPurchaseAmountChange,
            onPurchaseEvaluateClick = viewModel::onPurchaseEvaluateClick,
            onPurchaseBuyClick = viewModel::onPurchaseBuyClick,
            onPurchaseHoldClick = viewModel::onPurchaseHoldClick,
            onPurchasePrepareInVaultClick = {
                val result = uiState.purchaseResult
                viewModel.onPurchaseSheetDismiss()
                if (result != null) {
                    onPrepareInVault(result.title, result.price)
                }
            },
        )
    }

    if (showChallengeSheet) {
        ChallengeSettingsSheet(onDismiss = { showChallengeSheet = false })
    }
}
