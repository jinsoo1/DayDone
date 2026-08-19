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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneOffset
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
import java.time.LocalDate

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
                    text = "오늘 권장 금액",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                TodayDefenseCard(uiState = uiState)
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
                    onExtraIncomeClick = onExtraIncomeRowClick
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
    uiState: TodayUiState
) {
    val titleText = if (uiState.isTodayOverDefenseLine) {
        "오늘 권장보다 조금 더 썼어요"
    } else {
        "오늘 이만큼 남았어요"
    }

    val amountText = if (uiState.isTodayOverDefenseLine) {
        uiState.todayOverAmount.toMoneyText()
    } else {
        uiState.todayRemainingDefenseLine.toMoneyText()
    }

    val subMessage = if (uiState.isTodayOverDefenseLine) {
        "오늘 권장 금액 ${uiState.todayStartDefenseLine.toMoneyText()}보다 ${uiState.todayOverAmount.toMoneyText()} 더 썼어요."
    } else {
        "오늘 권장 금액 ${uiState.todayStartDefenseLine.toMoneyText()} 중 ${uiState.todayExpenseAmount.toMoneyText()}을 썼어요."
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
                    text = "이번 기간 ${uiState.remainingDays}일 남음",
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
                uiState.remainingDays == 1 -> "새 기간이 시작돼요"
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
                        text = "내일부터",
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
                text = "${periodStartLabel}부터 오늘까지 쓴 돈이 있다면",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "넣어야 오늘 쓸 수 있는 금액이 정확해져요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "넣기",
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
        title = { Text("이미 쓴 돈 넣기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${periodStartLabel}부터 어제까지 쓴 금액을 대략 넣어주세요. " +
                            "정확하지 않아도 괜찮아요 — 은행 앱을 슬쩍 보고 어림잡아도 충분해요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "저장하면 '이전 지출'로 기록되고, 월 탭 캘린더의 " +
                            "$periodStartLabel 내역에서 언제든 고칠 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("대략 쓴 금액") },
                    singleLine = true,
                    suffix = { Text("원") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("건너뛰기")
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
            text = "지난 기간 결산 리포트가 나왔어요",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "보기",
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
        NoSpendMode.FULL -> "완전 무지출"
        NoSpendMode.ESSENTIAL_ALLOWED -> "필수 지출 허용"
        NoSpendMode.CAP -> "하루 ${uiState.challengeCapAmount.toMoneyText()} 이하"
    }

    val statusText = when {
        uiState.challengeFinished ->
            "무지출 챌린지가 끝났어요 · ${uiState.challengeTargetDays}일 중 ${uiState.challengeSuccessDays}일 성공 🎉"
        uiState.challengeTodayOnTrack && uiState.challengeStreak >= 2 ->
            "오늘도 지갑이 쉬는 중이에요"
        uiState.challengeTodayOnTrack ->
            "오늘 지갑이 쉬는 중이에요"
        else ->
            "오늘은 지출이 있었어요. 내일 다시 이어가요"
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
                Text(
                    text = "무지출 챌린지 · $modeText",
                    style = MaterialTheme.typography.titleMedium
                )

                if (uiState.challengeStreak > 0 && !uiState.challengeFinished) {
                    Text(
                        text = "🔥 ${uiState.challengeStreak}일 연속",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = if (uiState.challengeFinished) {
                    "${uiState.challengeSuccessDays}일 성공 / ${uiState.challengeTargetDays}일 도전"
                } else {
                    "${uiState.challengeDayIndex}일째 · 성공 ${uiState.challengeSuccessDays}일 / ${uiState.challengeTargetDays}일 도전"
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
                        Text("그만 보기")
                    }
                    Button(
                        onClick = onRestartClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("새로 도전하기")
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
                    text = "이번 기간 요약",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (expanded) "접기" else "자세히",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            BudgetSummaryRow(
                title = "남은 순수 생활비",
                amountText = uiState.remainingPureBudget.toMoneyText()
            )

            Text(
                text = "저축과 고정비는 이미 생활비에서 제외해두었어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expanded) {
                HorizontalDivider()

                BudgetSummaryRow(
                    title = "월 예산",
                    amountText = uiState.monthlyIncome.toMoneyText()
                )

                if (uiState.extraIncomeAmount > 0L) {
                    BudgetSummaryRow(
                        title = "추가 수익",
                        amountText = "+${uiState.extraIncomeAmount.toMoneyText()}"
                    )
                }

                BudgetSummaryRow(
                    title = "사용 가능 예산",
                    amountText = uiState.totalAvailableBudget.toMoneyText()
                )

                if (uiState.scheduledDeductionTotalAmount > 0L) {
                    BudgetSummaryRow(
                        title = "차감 예정 금액",
                        amountText = "-${uiState.scheduledDeductionTotalAmount.toMoneyText()}"
                    )
                }

                if (uiState.pastExpenseAmount > 0L) {
                    BudgetSummaryRow(
                        title = "오늘 이전 지출",
                        amountText = "-${uiState.pastExpenseAmount.toMoneyText()}"
                    )
                }

                if (uiState.todayExpenseAmount > 0L) {
                    BudgetSummaryRow(
                        title = "오늘 지출",
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
                    text = "예정 차감",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (expanded) "접기" else "자세히",
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
                    text = "저축",
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
                    text = "고정비",
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
                text = "이번 기간에 나갈 돈은 이미 생활비에서 제외해두었어요.",
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
        ScheduledDeductionType.SAVING -> "저축"
        ScheduledDeductionType.FIXED -> "고정비"
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
                text = "${item.withdrawalDate.monthValue}월 ${item.withdrawalDate.dayOfMonth}일 · $typeText",
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
                text = if (item.isToday) "오늘" else item.weekText,
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
            text = "빠른 지출",
            style = MaterialTheme.typography.titleMedium
        )

        if (quickExpenses.isEmpty()) {
            OutlinedButton(
                onClick = onQuickExpenseAddClick
            ) {
                Text("자주 쓰는 지출 추가하기")
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
                                    contentDescription = "빠른 지출 삭제",
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
                            Text("+ 추가")
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
    onExtraIncomeClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (expenses.isEmpty() && extraIncomes.isEmpty() && scheduledDeductions.isEmpty()) {
                Text(
                    text = "아직 기록된 내역이 없어요.",
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
                        text = "총 지출",
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
        ScheduledDeductionType.SAVING -> "저축"
        ScheduledDeductionType.FIXED -> "고정비"
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
                text = "${item.title} 예정",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "이미 생활비에서 제외해두었어요.",
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
                text = "${income.title} (수익)",
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
        Text("$label: ${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일")
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
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("취소")
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
                text = if (isEditing) "지출 수정" else "지출 추가",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("지출명")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("금액")
                },
                singleLine = true,
                suffix = {
                    Text("원")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            DateSelectorField(
                label = "날짜",
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
                            text = "필수 지출",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "무지출 챌린지에서 이 지출은 허용돼요 (교통비, 약값 등)",
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
                Text(if (isEditing) "수정하기" else "추가하기")
            }

            if (isEditing) {
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "삭제하기",
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
                text = "예산 설정",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = monthlyIncomeInput,
                onValueChange = onMonthlyIncomeChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("월 수입")
                },
                singleLine = true,
                suffix = {
                    Text("원")
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
                    Text("예산 시작일")
                },
                singleLine = true,
                suffix = {
                    Text("일")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text("1~31일 사이로 입력해 주세요. 31일은 매월 말일로 계산돼요.")
                }
            )

            NoticeBox(
                title = "시작일을 바꾸면 모든 기간이 다시 나뉘어요",
                lines = listOf(
                    "지난 기록을 포함한 모든 달이 새 시작일 기준으로 다시 계산돼요.",
                    "날짜에 따라 지출·저축/고정비가 옆 달 기간으로 이동해 보일 수 있어요."
                )
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = monthlyIncomeInput.toLongOrNull() != null &&
                        monthlyIncomeInput.toLongOrNull()!! > 0L &&
                        budgetStartDayInput.toIntOrNull() in 1..31
            ) {
                Text("저장하기")
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
        ?: "0원"

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
                text = if (isEditing) "저축 / 고정비 수정" else "저축 / 고정비 추가",
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
                        Text("저축")
                    }
                )

                FilterChip(
                    selected = selectedType == ScheduledDeductionType.FIXED,
                    onClick = {
                        onTypeChange(ScheduledDeductionType.FIXED)
                    },
                    label = {
                        Text("고정비")
                    }
                )
            }

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("항목명")
                },
                placeholder = {
                    Text(
                        if (selectedType == ScheduledDeductionType.SAVING) {
                            "예: 적금"
                        } else {
                            "예: 보험료"
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
                    Text("금액")
                },
                placeholder = {
                    Text("예: 700000")
                },
                singleLine = true,
                suffix = {
                    Text("원")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text("입력한 금액: $amountPreview")
                }
            )

            OutlinedTextField(
                value = withdrawalDayInput,
                onValueChange = onWithdrawalDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("출금일")
                },
                placeholder = {
                    Text("예: 26")
                },
                singleLine = true,
                suffix = {
                    Text("일")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    Text("매월 1~31일 사이로 입력해 주세요.")
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
                Text(if (isEditing) "수정하기" else "저장하기")
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = { showEndConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("이번 달까지만 하고 종료")
                }

                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "삭제하기",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text("이번 달까지만 하고 종료할까요?") },
            text = {
                Text("적금 만기나 완납처럼 이제 안 나가는 항목에 사용해요. 이번 달까지는 그대로 반영되고, 다음 달부터 예정 차감에서 사라져요. 지난 기록은 남아요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndConfirm = false
                    onEndClick()
                }) {
                    Text("종료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("삭제할까요?") },
            text = {
                Text("이 항목이 모든 기간(지난 달 포함)에서 사라지고 되돌릴 수 없어요. 이번 달까지만 쓰고 끝내려면 '종료'를 사용하세요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소")
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
                text = "빠른 지출 추가",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "자주 쓰는 지출을 빠르게 기록해볼까요?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("항목명")
                },
                placeholder = {
                    Text("예: 식비, 카페, 교통")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("금액")
                },
                placeholder = {
                    Text("예: 8000")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    val amount = amountInput.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        Text("기록될 금액: ${amount.toMoneyText()}")
                    }
                }
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text("저장")
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
                text = if (isEditing) "추가 수익 수정" else "추가 수익 기록",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "이번 예산 기간에 추가로 들어온 돈을 더해둘게요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("항목명")
                },
                placeholder = {
                    Text("예: 환급금, 중고거래, 부수입")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("금액")
                },
                placeholder = {
                    Text("예: 100000")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    val amount = amountInput.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        Text("더해질 금액: ${amount.toMoneyText()}")
                    }
                }
            )

            OutlinedTextField(
                value = memoInput,
                onValueChange = onMemoChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("메모")
                },
                placeholder = {
                    Text("선택 입력")
                },
                minLines = 2,
                maxLines = 3
            )

            DateSelectorField(
                label = "날짜",
                date = dateInput,
                onDateChange = onDateChange
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = titleInput.isNotBlank() &&
                        (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(if (isEditing) "수정하기" else "저장")
            }

            if (isEditing) {
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "삭제하기",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}