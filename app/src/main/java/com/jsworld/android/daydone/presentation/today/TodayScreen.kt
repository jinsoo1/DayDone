package com.jsworld.android.daydone.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.presentation.today.model.QuickExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.ScheduledDeductionSummaryUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayDateChipUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExtraIncomeUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayScheduledDeductionUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayUiState
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.NoticeBox
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    uiState: TodayUiState,
    onDateClick: (LocalDate) -> Unit,
    onQuickExpenseClick: (QuickExpenseUiModel) -> Unit,

    onExpenseRowClick: (TodayExpenseUiModel) -> Unit,
    onExpenseInputDismiss: () -> Unit,
    onExpenseTitleChange: (String) -> Unit,
    onExpenseAmountChange: (String) -> Unit,
    onExpenseDateChange: (LocalDate) -> Unit,
    onExpenseEssentialChange: (Boolean) -> Unit,
    onChallengeRestartClick: () -> Unit,
    onChallengeDismissClick: () -> Unit,
    onLastReportClick: () -> Unit = {},
    onPreJoinBannerClick: () -> Unit = {},
    onPreJoinAmountChange: (String) -> Unit = {},
    onPreJoinDialogDismiss: () -> Unit = {},
    onPreJoinSave: () -> Unit = {},
    onPreJoinSkip: () -> Unit = {},
    onAddExpenseClick: () -> Unit,
    onDeleteExpenseClick: () -> Unit,

    onBudgetSettingDismiss: () -> Unit,
    onMonthlyIncomeChange: (String) -> Unit,
    onBudgetStartDayChange: (String) -> Unit,
    onSaveBudgetSettingClick: () -> Unit,

    onScheduledDeductionRowClick: (Long) -> Unit,
    onScheduledDeductionInputDismiss: () -> Unit,
    onScheduledDeductionTitleChange: (String) -> Unit,
    onScheduledDeductionAmountChange: (String) -> Unit,
    onScheduledDeductionWithdrawalDayChange: (String) -> Unit,
    onScheduledDeductionTypeChange: (ScheduledDeductionType) -> Unit,
    onSaveScheduledDeductionClick: () -> Unit,
    onEndScheduledDeductionClick: () -> Unit,
    onDeleteScheduledDeductionClick: () -> Unit,

    onQuickExpenseAddClick: () -> Unit,
    onQuickExpenseInputDismiss: () -> Unit,
    onQuickExpenseTitleChange: (String) -> Unit,
    onQuickExpenseAmountChange: (String) -> Unit,
    onSaveQuickExpenseClick: () -> Unit,
    onDeleteQuickExpenseClick: (Long) -> Unit,

    onExtraIncomeRowClick: (Long) -> Unit,
    onExtraIncomeInputDismiss: () -> Unit,
    onExtraIncomeTitleChange: (String) -> Unit,
    onExtraIncomeAmountChange: (String) -> Unit,
    onExtraIncomeMemoChange: (String) -> Unit,
    onExtraIncomeDateChange: (LocalDate) -> Unit,
    onSaveExtraIncomeClick: () -> Unit,
    onDeleteExtraIncomeClick: () -> Unit,

    onOpenLedgerClick: () -> Unit = {},
    onPrepareSurplusClick: () -> Unit = {},

    onPurchaseSheetDismiss: () -> Unit = {},
    onPurchaseTitleChange: (String) -> Unit = {},
    onPurchaseAmountChange: (String) -> Unit = {},
    onPurchaseEvaluateClick: () -> Unit = {},
    onPurchaseBuyClick: () -> Unit = {},
    onPurchaseHoldClick: () -> Unit = {},
    onPurchasePrepareInVaultClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.today_oneul_gweonjang_geumaeg),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                TodayDefenseCard(
                    uiState = uiState,
                    onPrepareSurplusClick = onPrepareSurplusClick
                )
            }

            if (uiState.showPreJoinBanner) {
                item {
                    PreJoinBanner(
                        periodStartLabel = uiState.periodStartLabel,
                        onClick = onPreJoinBannerClick
                    )
                }
            }

            if (uiState.lastPeriodReportMonth != null) {
                item {
                    LastReportBanner(onClick = onLastReportClick)
                }
            }

            if (uiState.challengeEnabled) {
                item {
                    NoSpendChallengeCard(
                        uiState = uiState,
                        onRestartClick = onChallengeRestartClick,
                        onDismissClick = onChallengeDismissClick
                    )
                }
            }

            item {
                DateChipRow(
                    dateChips = uiState.dateChips,
                    onDateClick = onDateClick
                )
            }

            item {
                SelectedDateSection(
                    title = uiState.selectedDateTitle,
                    expenses = uiState.selectedDateExpenses,
                    extraIncomes = uiState.selectedDateExtraIncomes,
                    scheduledDeductions = uiState.selectedDateScheduledDeductions,
                    onExpenseClick = onExpenseRowClick,
                    onExtraIncomeClick = onExtraIncomeRowClick,
                    onOpenLedgerClick = onOpenLedgerClick
                )
            }

            item {
                QuickExpenseRow(
                    quickExpenses = uiState.quickExpenses,
                    onQuickExpenseClick = onQuickExpenseClick,
                    onQuickExpenseAddClick = onQuickExpenseAddClick,
                    onDeleteQuickExpenseClick = onDeleteQuickExpenseClick
                )
            }

            item {
                BudgetSummaryCard(uiState = uiState)
            }

            item {
                ScheduledDeductionSummaryCard(
                    savingAmount = uiState.scheduledSavingAmount,
                    fixedAmount = uiState.fixedExpenseAmount,
                    items = uiState.scheduledDeductionSummaries,
                    onItemClick = onScheduledDeductionRowClick
                )
            }
        }
    }

    if (uiState.isPreJoinDialogVisible) {
        PreJoinAmountDialog(
            periodStartLabel = uiState.periodStartLabel,
            amountInput = uiState.preJoinAmountInput,
            onAmountChange = onPreJoinAmountChange,
            onSave = onPreJoinSave,
            onSkip = onPreJoinSkip,
            onDismiss = onPreJoinDialogDismiss
        )
    }

    if (uiState.isExpenseInputSheetVisible) {
        ExpenseInputBottomSheet(
            isEditing = uiState.editingExpenseId != null,
            titleInput = uiState.expenseTitleInput,
            amountInput = uiState.expenseAmountInput,
            dateInput = uiState.expenseDateInput,
            isEssentialInput = uiState.expenseEssentialInput,
            showEssential = uiState.showEssentialCheckbox,
            onTitleChange = onExpenseTitleChange,
            onAmountChange = onExpenseAmountChange,
            onDateChange = onExpenseDateChange,
            onEssentialChange = onExpenseEssentialChange,
            onDismiss = onExpenseInputDismiss,
            onAddClick = onAddExpenseClick,
            onDeleteClick = onDeleteExpenseClick
        )
    }

    if (uiState.isBudgetSettingSheetVisible) {
        BudgetSettingBottomSheet(
            monthlyIncomeInput = uiState.monthlyIncomeInput,
            budgetStartDayInput = uiState.budgetStartDayInput,
            onMonthlyIncomeChange = onMonthlyIncomeChange,
            onBudgetStartDayChange = onBudgetStartDayChange,
            onDismiss = onBudgetSettingDismiss,
            onSaveClick = onSaveBudgetSettingClick
        )
    }

    if (uiState.isScheduledDeductionSheetVisible) {
        ScheduledDeductionInputBottomSheet(
            isEditing = uiState.editingScheduledDeductionId != null,
            titleInput = uiState.scheduledDeductionTitleInput,
            amountInput = uiState.scheduledDeductionAmountInput,
            withdrawalDayInput = uiState.scheduledDeductionWithdrawalDayInput,
            selectedType = uiState.scheduledDeductionTypeInput,
            onTitleChange = onScheduledDeductionTitleChange,
            onAmountChange = onScheduledDeductionAmountChange,
            onWithdrawalDayChange = onScheduledDeductionWithdrawalDayChange,
            onTypeChange = onScheduledDeductionTypeChange,
            onDismiss = onScheduledDeductionInputDismiss,
            onSaveClick = onSaveScheduledDeductionClick,
            onEndClick = onEndScheduledDeductionClick,
            onDeleteClick = onDeleteScheduledDeductionClick
        )
    }

    if (uiState.isQuickExpenseInputSheetVisible) {
        QuickExpenseInputBottomSheet(
            titleInput = uiState.quickExpenseTitleInput,
            amountInput = uiState.quickExpenseAmountInput,
            onTitleChange = onQuickExpenseTitleChange,
            onAmountChange = onQuickExpenseAmountChange,
            onSaveClick = onSaveQuickExpenseClick,
            onDismiss = onQuickExpenseInputDismiss
        )
    }

    if (uiState.isPurchaseSheetVisible) {
        PurchaseDecisionSheet(
            titleInput = uiState.purchaseTitleInput,
            amountInput = uiState.purchaseAmountInput,
            result = uiState.purchaseResult,
            heldDone = uiState.purchaseHeldDone,
            onTitleChange = onPurchaseTitleChange,
            onAmountChange = onPurchaseAmountChange,
            onEvaluateClick = onPurchaseEvaluateClick,
            onBuyClick = onPurchaseBuyClick,
            onHoldClick = onPurchaseHoldClick,
            onPrepareInVaultClick = onPurchasePrepareInVaultClick,
            onDismiss = onPurchaseSheetDismiss
        )
    }

    if (uiState.isExtraIncomeInputSheetVisible) {
        ExtraIncomeInputBottomSheet(
            isEditing = uiState.editingExtraIncomeId != null,
            titleInput = uiState.extraIncomeTitleInput,
            amountInput = uiState.extraIncomeAmountInput,
            memoInput = uiState.extraIncomeMemoInput,
            dateInput = uiState.extraIncomeDateInput,
            onTitleChange = onExtraIncomeTitleChange,
            onAmountChange = onExtraIncomeAmountChange,
            onMemoChange = onExtraIncomeMemoChange,
            onDateChange = onExtraIncomeDateChange,
            onSaveClick = onSaveExtraIncomeClick,
            onDismiss = onExtraIncomeInputDismiss,
            onDeleteClick = onDeleteExtraIncomeClick
        )
    }
}

@Composable
private fun TodayDefenseCard(
    uiState: TodayUiState,
    onPrepareSurplusClick: () -> Unit
) {
    val titleText = if (uiState.isTodayOverDefenseLine) {
        stringResource(R.string.today_oneul_gweonjangboda_jogeum_deo)
    } else {
        stringResource(R.string.today_oneul_imankeum_namasseoyo)
    }

    val amountText = if (uiState.isTodayOverDefenseLine) {
        uiState.todayOverAmount.toMoneyText()
    } else {
        uiState.todayRemainingDefenseLine.toMoneyText()
    }

    val subMessage = if (uiState.isTodayOverDefenseLine) {
        stringResource(R.string.today_oneul_gweonjang_geumaeg_boda, uiState.todayStartDefenseLine.toMoneyText(), uiState.todayOverAmount.toMoneyText())
    } else {
        stringResource(R.string.today_oneul_gweonjang_geumaeg_jung, uiState.todayStartDefenseLine.toMoneyText(), uiState.todayExpenseAmount.toMoneyText())
    }

    val accentColor = if (uiState.isTodayOverDefenseLine) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val usedFraction = if (uiState.todayStartDefenseLine > 0L) {
        (uiState.todayExpenseAmount.toFloat() / uiState.todayStartDefenseLine.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = amountText,
                style = MaterialTheme.typography.displaySmall,
                color = accentColor
            )

            if (uiState.remainingDays > 0) {
                Text(
                    text = stringResource(R.string.today_ibeon_gigan_il_nameum, uiState.remainingDays),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }

            LinearProgressIndicator(
                progress = { usedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Text(
                text = uiState.message,
                style = MaterialTheme.typography.bodyMedium
            )

            // 막바지 여유분 → 금고 추가 시트에 금액 프리필 (살까 말까와 같은 경로, 새 저장 경로 없음)
            if (uiState.periodEndSurplus != null) {
                TextButton(
                    onClick = onPrepareSurplusClick,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.today_geumgoe_junbihagi),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (uiState.todayExpenseAmount > 0L) {
                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            // 내일 권장 금액 — 재분배를 숫자로 보여준다.
            // 기간 마지막 날에는 나눌 내일이 없으니(null) 금액 대신 새 기간 안내를 띄운다.
            val tomorrowText = when {
                uiState.tomorrowRecommended != null -> uiState.tomorrowRecommended.toMoneyText()
                uiState.remainingDays == 1 -> stringResource(R.string.today_sae_gigani_sijagdwaeyo)
                else -> null
            }

            if (tomorrowText != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_naeilbuteo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Text(
                        text = tomorrowText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PreJoinBanner(
    periodStartLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "💡", style = MaterialTheme.typography.bodyMedium)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = stringResource(R.string.today_buteo_oneulkkaji_sseun_doni, periodStartLabel),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.today_neoheoya_oneul_sseul_su),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.today_neohgi),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PreJoinAmountDialog(
    periodStartLabel: String,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.today_imi_sseun_don_neohgi)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.today_buteo_eojekkaji_sseun_geumaegeul, periodStartLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.today_jeojanghamyeon_ijeon_jichul_ro, periodStartLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.today_daeryag_sseun_geumaeg)) },
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.today_weon)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(stringResource(R.string.today_jeojang))
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.today_geonneottwigi))
            }
        }
    )
}

@Composable
private fun LastReportBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "📊", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.today_jinan_gigan_gyeolsan_ripoteuga),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.today_bogi),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NoSpendChallengeCard(
    uiState: TodayUiState,
    onRestartClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val modeText = when (uiState.challengeMode) {
        NoSpendMode.FULL -> stringResource(R.string.today_wanjeon_mujichul)
        NoSpendMode.ESSENTIAL_ALLOWED -> stringResource(R.string.today_pilsu_jichul_heoyong)
        NoSpendMode.CAP -> stringResource(R.string.today_haru_iha, uiState.challengeCapAmount.toMoneyText())
    }

    val statusText = when {
        uiState.challengeFinished ->
            stringResource(R.string.today_mujichul_chaelrinjiga_kkeutnasseoyo_il, uiState.challengeTargetDays, uiState.challengeSuccessDays)
        uiState.challengeTodayOnTrack && uiState.challengeStreak >= 2 ->
            stringResource(R.string.today_oneuldo_jigabi_swineun_jungieyo)
        uiState.challengeTodayOnTrack ->
            stringResource(R.string.today_oneul_jigabi_swineun_jungieyo)
        else ->
            stringResource(R.string.today_oneuleun_jichuli_isseosseoyo_naeil)
    }

    val progress = if (uiState.challengeTargetDays > 0) {
        (uiState.challengeSuccessDays.toFloat() / uiState.challengeTargetDays.toFloat())
            .coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 제목이 길면(일본어) 연속 표시가 밀려 한 글자씩 줄바꿈되던 것 — 제목이 남는 폭을
                // 차지하고, 연속 표시는 한 줄로 고정한다.
                Text(
                    text = stringResource(R.string.today_mujichul_chaelrinji, modeText),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (uiState.challengeStreak > 0 && !uiState.challengeFinished) {
                    Text(
                        text = stringResource(R.string.today_il_yeonsog, uiState.challengeStreak),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Text(
                text = if (uiState.challengeFinished) {
                    stringResource(R.string.today_il_seonggong_il_dojeon, uiState.challengeSuccessDays, uiState.challengeTargetDays)
                } else {
                    stringResource(R.string.today_iljjae_seonggong_il_il, uiState.challengeDayIndex, uiState.challengeSuccessDays, uiState.challengeTargetDays)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (uiState.challengeTodayOnTrack || uiState.challengeFinished) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (uiState.challengeFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.today_geuman_bogi))
                    }
                    Button(
                        onClick = onRestartClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.today_saero_dojeonhagi))
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    uiState: TodayUiState
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_ibeon_gigan_yoyag),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (expanded) stringResource(R.string.today_jeobgi) else stringResource(R.string.today_jasehi),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            BudgetSummaryRow(
                title = stringResource(R.string.today_nameun_sunsu_saenghwalbi),
                amountText = uiState.remainingPureBudget.toMoneyText()
            )

            Text(
                text = stringResource(R.string.today_jeochuggwa_gojeongbineun_imi_saenghwalbieseo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expanded) {
                HorizontalDivider()

                BudgetSummaryRow(
                    title = stringResource(R.string.today_weol_yesan),
                    amountText = uiState.monthlyIncome.toMoneyText()
                )

                if (uiState.extraIncomeAmount > 0L) {
                    BudgetSummaryRow(
                        title = stringResource(R.string.today_chuga_suig),
                        amountText = "+${uiState.extraIncomeAmount.toMoneyText()}"
                    )
                }

                BudgetSummaryRow(
                    title = stringResource(R.string.today_sayong_ganeung_yesan),
                    amountText = uiState.totalAvailableBudget.toMoneyText()
                )

                if (uiState.scheduledDeductionTotalAmount > 0L) {
                    BudgetSummaryRow(
                        title = stringResource(R.string.today_chagam_yejeong_geumaeg),
                        amountText = "-${uiState.scheduledDeductionTotalAmount.toMoneyText()}"
                    )
                }

                if (uiState.pastExpenseAmount > 0L) {
                    BudgetSummaryRow(
                        title = stringResource(R.string.today_oneul_ijeon_jichul),
                        amountText = "-${uiState.pastExpenseAmount.toMoneyText()}"
                    )
                }

                if (uiState.todayExpenseAmount > 0L) {
                    BudgetSummaryRow(
                        title = stringResource(R.string.today_oneul_jichul),
                        amountText = "-${uiState.todayExpenseAmount.toMoneyText()}"
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetSummaryRow(
    title: String,
    amountText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ScheduledDeductionSummaryCard(
    savingAmount: Long,
    fixedAmount: Long,
    items: List<ScheduledDeductionSummaryUiModel>,
    onItemClick: (Long) -> Unit
) {
    if (items.isEmpty()) {
        return
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_yejeong_chagam),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (expanded) stringResource(R.string.today_jeobgi) else stringResource(R.string.today_jasehi),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.today_jeochug),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = savingAmount.toMoneyText(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.today_gojeongbi),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = fixedAmount.toMoneyText(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (expanded) {
                HorizontalDivider()

                items.forEach { item ->
                    ScheduledDeductionSummaryRow(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }

            Text(
                text = stringResource(R.string.today_ibeon_gigane_nagal_doneun),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScheduledDeductionSummaryRow(
    item: ScheduledDeductionSummaryUiModel,
    onClick: () -> Unit
) {
    val typeText = when (item.type) {
        ScheduledDeductionType.SAVING -> stringResource(R.string.today_jeochug)
        ScheduledDeductionType.FIXED -> stringResource(R.string.today_gojeongbi)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.today_weol_il, item.withdrawalDate.monthValue, item.withdrawalDate.dayOfMonth, typeText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "-${item.amount.toMoneyText()}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DateChipRow(
    dateChips: List<TodayDateChipUiModel>,
    onDateClick: (LocalDate) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(
            items = dateChips,
            key = { it.date.toString() }
        ) { item ->
            DateChip(
                item = item,
                onClick = { onDateClick(item.date) }
            )
        }
    }
}

@Composable
private fun DateChip(
    item: TodayDateChipUiModel,
    onClick: () -> Unit
) {
    val containerColor =
        if (item.isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .width(56.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (item.isToday) stringResource(R.string.today_oneul) else item.weekText,
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = item.dayText,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.hasExpense) {
                    SmallDot()
                }

                if (item.hasScheduledDeduction) {
                    SmallDot()
                }
            }
        }
    }
}

@Composable
private fun SmallDot() {
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    )
}


@Composable
private fun QuickExpenseRow(
    quickExpenses: List<QuickExpenseUiModel>,
    onQuickExpenseClick: (QuickExpenseUiModel) -> Unit,
    onQuickExpenseAddClick: () -> Unit,
    onDeleteQuickExpenseClick: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.today_ppareun_jichul),
            style = MaterialTheme.typography.titleMedium
        )

        if (quickExpenses.isEmpty()) {
            OutlinedButton(
                onClick = onQuickExpenseAddClick
            ) {
                Text(stringResource(R.string.today_jaju_sseuneun_jichul_chugahagi))
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = quickExpenses,
                    key = { it.id }
                ) { item ->
                    InputChip(
                        selected = false,
                        onClick = {
                            onQuickExpenseClick(item)
                        },
                        label = {
                            Text("${item.title} ${item.amount.toMoneyText()}")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onDeleteQuickExpenseClick(item.id)
                                },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.today_ppareun_jichul_sagje),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    )
                }

                item {
                    AssistChip(
                        onClick = onQuickExpenseAddClick,
                        label = {
                            Text(stringResource(R.string.today_chuga))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun SelectedDateSection(
    title: String,
    expenses: List<TodayExpenseUiModel>,
    extraIncomes: List<TodayExtraIncomeUiModel>,
    scheduledDeductions: List<TodayScheduledDeductionUiModel>,
    onExpenseClick: (TodayExpenseUiModel) -> Unit,
    onExtraIncomeClick: (Long) -> Unit,
    onOpenLedgerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 하루치만 보여주는 섹션이라, 여기서 기간 전체 내역으로 넘어가는 게 가장 자연스럽다
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = stringResource(R.string.today_jeonche_bogi),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenLedgerClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (expenses.isEmpty() && extraIncomes.isEmpty() && scheduledDeductions.isEmpty()) {
                Text(
                    text = stringResource(R.string.today_ajig_girogdoen_naeyeogi_eobseoyo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            scheduledDeductions.forEach { item ->
                SelectedDateScheduledDeductionRow(item = item)
            }

            extraIncomes.forEach { income ->
                SelectedDateExtraIncomeRow(
                    income = income,
                    onClick = { onExtraIncomeClick(income.id) }
                )
            }

            expenses.forEach { expense ->
                SelectedDateExpenseRow(
                    expense = expense,
                    onClick = { onExpenseClick(expense) }
                )
            }

            if (expenses.isNotEmpty()) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_chong_jichul),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "-${expenses.sumOf { it.amount }.toMoneyText()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDateScheduledDeductionRow(
    item: TodayScheduledDeductionUiModel
) {
    val typeText = when (item.type) {
        ScheduledDeductionType.SAVING -> stringResource(R.string.today_jeochug)
        ScheduledDeductionType.FIXED -> stringResource(R.string.today_gojeongbi)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.today_yejeong, item.title),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = stringResource(R.string.today_imi_saenghwalbieseo_jeoehaedueosseoyo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = typeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "-${item.amount.toMoneyText()}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SelectedDateExpenseRow(
    expense: TodayExpenseUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = expense.title,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "-${expense.amount.toMoneyText()}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SelectedDateExtraIncomeRow(
    income: TodayExtraIncomeUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.today_suig, income.title),
                style = MaterialTheme.typography.bodyMedium
            )

            if (!income.memo.isNullOrBlank()) {
                Text(
                    text = income.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "+${income.amount.toMoneyText()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.today_nyeon_weol_il, label, date.year, date.monthValue, date.dayOfMonth))
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onDateChange(picked)
                        }
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.today_hwagin))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(R.string.today_chwiso))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpenseInputBottomSheet(
    isEditing: Boolean,
    titleInput: String,
    amountInput: String,
    dateInput: LocalDate,
    isEssentialInput: Boolean = false,
    showEssential: Boolean = false,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onEssentialChange: (Boolean) -> Unit = {},
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.today_jichul_sujeong) else stringResource(R.string.today_jichul_chuga),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_jichulmyeong))
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_geumaeg))
                },
                singleLine = true,
                suffix = {
                    Text(stringResource(R.string.today_weon))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            DateSelectorField(
                label = stringResource(R.string.today_naljja),
                date = dateInput,
                onDateChange = onDateChange
            )

            if (showEssential) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEssentialChange(!isEssentialInput) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEssentialInput,
                        onCheckedChange = onEssentialChange
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.today_pilsu_jichul),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.today_mujichul_chaelrinjieseo_i_jichuleun),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(if (isEditing) stringResource(R.string.today_sujeonghagi) else stringResource(R.string.today_chugahagi))
            }

            if (isEditing) {
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.today_sagjehagi),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSettingBottomSheet(
    monthlyIncomeInput: String,
    budgetStartDayInput: String,
    onMonthlyIncomeChange: (String) -> Unit,
    onBudgetStartDayChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.today_yesan_seoljeong),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = monthlyIncomeInput,
                onValueChange = onMonthlyIncomeChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_weol_suib))
                },
                singleLine = true,
                suffix = {
                    Text(stringResource(R.string.today_weon))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            OutlinedTextField(
                value = budgetStartDayInput,
                onValueChange = onBudgetStartDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_yesan_sijagil))
                },
                singleLine = true,
                suffix = {
                    Text(stringResource(R.string.today_il))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text(stringResource(R.string.today_1_31il_sairo_ibryeoghae))
                }
            )

            NoticeBox(
                title = stringResource(R.string.today_sijagileul_bakkumyeon_modeun_gigani),
                lines = listOf(
                    stringResource(R.string.today_jinan_girogeul_pohamhan_modeun),
                    stringResource(R.string.today_naljjae_ttara_jichul_jeochug)
                )
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = monthlyIncomeInput.toLongOrNull() != null &&
                        monthlyIncomeInput.toLongOrNull()!! > 0L &&
                        budgetStartDayInput.toIntOrNull() in 1..31
            ) {
                Text(stringResource(R.string.today_jeojanghagi))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduledDeductionInputBottomSheet(
    isEditing: Boolean,
    titleInput: String,
    amountInput: String,
    withdrawalDayInput: String,
    selectedType: ScheduledDeductionType,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onWithdrawalDayChange: (String) -> Unit,
    onTypeChange: (ScheduledDeductionType) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    onEndClick: () -> Unit = {},
    onDeleteClick: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var showEndConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val amountPreview = amountInput
        .toLongOrNull()
        ?.toMoneyText()
        ?: stringResource(R.string.today_0weon)

    val withdrawalDay = withdrawalDayInput.toIntOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.today_jeochug_gojeongbi_sujeong) else stringResource(R.string.today_jeochug_gojeongbi_chuga),
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == ScheduledDeductionType.SAVING,
                    onClick = {
                        onTypeChange(ScheduledDeductionType.SAVING)
                    },
                    label = {
                        Text(stringResource(R.string.today_jeochug))
                    }
                )

                FilterChip(
                    selected = selectedType == ScheduledDeductionType.FIXED,
                    onClick = {
                        onTypeChange(ScheduledDeductionType.FIXED)
                    },
                    label = {
                        Text(stringResource(R.string.today_gojeongbi))
                    }
                )
            }

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_hangmogmyeong))
                },
                placeholder = {
                    Text(
                        if (selectedType == ScheduledDeductionType.SAVING) {
                            stringResource(R.string.today_ye_jeoggeum)
                        } else {
                            stringResource(R.string.today_ye_boheomryo)
                        }
                    )
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_geumaeg))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_700000))
                },
                singleLine = true,
                suffix = {
                    Text(stringResource(R.string.today_weon))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text(stringResource(R.string.today_ibryeoghan_geumaeg, amountPreview))
                }
            )

            OutlinedTextField(
                value = withdrawalDayInput,
                onValueChange = onWithdrawalDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_chulgeumil))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_26))
                },
                singleLine = true,
                suffix = {
                    Text(stringResource(R.string.today_il))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text(stringResource(R.string.today_maeweol_1_31il_sairo))
                }
            )

            val canSave =
                titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L &&
                        withdrawalDay in 1..31

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(if (isEditing) stringResource(R.string.today_sujeonghagi) else stringResource(R.string.today_jeojanghagi))
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = { showEndConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.today_ibeon_dalkkajiman_hago_jongryo))
                }

                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.today_sagjehagi),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text(stringResource(R.string.today_ibeon_dalkkajiman_hago_jongryohalkkayo)) },
            text = {
                Text(stringResource(R.string.today_jeoggeum_mangina_wannabcheoreom_ije))
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndConfirm = false
                    onEndClick()
                }) {
                    Text(stringResource(R.string.today_jongryo))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) {
                    Text(stringResource(R.string.today_chwiso))
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.today_sagjehalkkayo)) },
            text = {
                Text(stringResource(R.string.today_i_hangmogi_modeun_gigan))
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.today_sagje), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.today_chwiso))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickExpenseInputBottomSheet(
    titleInput: String,
    amountInput: String,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.today_ppareun_jichul_chuga),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.today_jaju_sseuneun_jichuleul_ppareuge),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_hangmogmyeong))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_sigbi_kape_gyotong))
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_geumaeg))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_8000))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    val amount = amountInput.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        Text(stringResource(R.string.today_girogdoel_geumaeg, amount.toMoneyText()))
                    }
                }
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(stringResource(R.string.today_jeojang))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExtraIncomeInputBottomSheet(
    isEditing: Boolean,
    titleInput: String,
    amountInput: String,
    memoInput: String,
    dateInput: LocalDate,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) stringResource(R.string.today_chuga_suig_sujeong) else stringResource(R.string.today_chuga_suig_girog),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(R.string.today_ibeon_yesan_gigane_chugaro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_hangmogmyeong))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_hwangeubgeum_junggogeorae_busuib))
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_geumaeg))
                },
                placeholder = {
                    Text(stringResource(R.string.today_ye_100000))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    val amount = amountInput.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        Text(stringResource(R.string.today_deohaejil_geumaeg, amount.toMoneyText()))
                    }
                }
            )

            OutlinedTextField(
                value = memoInput,
                onValueChange = onMemoChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.today_memo))
                },
                placeholder = {
                    Text(stringResource(R.string.today_seontaeg_ibryeog))
                },
                minLines = 2,
                maxLines = 3
            )

            DateSelectorField(
                label = stringResource(R.string.today_naljja),
                date = dateInput,
                onDateChange = onDateChange
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(if (isEditing) stringResource(R.string.today_sujeonghagi) else stringResource(R.string.today_jeojang))
            }

            if (isEditing) {
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.today_sagjehagi),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}