package com.jsworld.android.daydone.presentation.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.presentation.monthly.model.MonthViewMode
import com.jsworld.android.daydone.presentation.monthly.model.MonthlyDayCellUiModel
import com.jsworld.android.daydone.presentation.monthly.model.MonthlyUiState
import com.jsworld.android.daydone.presentation.today.model.ScheduledDeductionSummaryUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExtraIncomeUiModel
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.theme.DayDoneAccent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(
    uiState: MonthlyUiState,
    onReportClick: () -> Unit = {},
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToCurrentMonth: () -> Unit = {},
    onDateClick: (LocalDate) -> Unit,
    onEditBudgetClick: () -> Unit,
    onBudgetInputChange: (String) -> Unit,
    onBudgetSheetDismiss: () -> Unit,
    onSaveBudgetClick: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onExtraIncomeClick: (Long) -> Unit,
    onScheduledDeductionClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DayDoneTopBar(
            title = "월별 예산",
            actions = {
                if (uiState.mode != MonthViewMode.CURRENT) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable(onClick = onGoToCurrentMonth)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "이번 달로",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthNavigator(
                    monthTitle = uiState.monthTitle,
                    periodText = uiState.periodText,
                    mode = uiState.mode,
                    canGoPrevious = uiState.canGoPrevious,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
            }

            item {
                MonthlySummaryCard(
                    uiState = uiState,
                    onEditBudgetClick = onEditBudgetClick
                )
            }

            // 진행 중: 페이스 리포트 / 지난 달: 결산 리포트
            if (uiState.mode == MonthViewMode.CURRENT) {
                item {
                    ReportEntryCard(
                        title = "이번 기간 리포트",
                        subtitle = "지금 페이스가 어떤지 확인해보세요",
                        onClick = onReportClick
                    )
                }
            } else if (uiState.mode == MonthViewMode.PAST) {
                item {
                    ReportEntryCard(
                        title = "기간 결산 리포트",
                        subtitle = "이 기간을 어떻게 보냈는지 돌아봐요",
                        onClick = onReportClick
                    )
                }
            }

            item {
                MonthCalendarCard(
                    weeks = uiState.calendarWeeks,
                    onDateClick = onDateClick
                )
            }

            item {
                SelectedDateCard(
                    title = uiState.selectedDateTitle,
                    expenses = uiState.selectedDateExpenses,
                    extraIncomes = uiState.selectedDateExtraIncomes,
                    scheduledDeductions = uiState.selectedDateScheduledDeductions,
                    onExpenseClick = onExpenseClick,
                    onExtraIncomeClick = onExtraIncomeClick,
                    onScheduledDeductionClick = onScheduledDeductionClick
                )
            }

            item {
                MonthlyScheduledDeductionCard(
                    items = uiState.scheduledDeductionSummaries,
                    onItemClick = onScheduledDeductionClick
                )
            }
        }
    }

    if (uiState.isBudgetSheetVisible) {
        BudgetInputBottomSheet(
            monthTitle = uiState.monthTitle,
            budgetInput = uiState.budgetInput,
            onBudgetInputChange = onBudgetInputChange,
            onSaveClick = onSaveBudgetClick,
            onDismiss = onBudgetSheetDismiss
        )
    }
}

@Composable
private fun MonthNavigator(
    monthTitle: String,
    periodText: String,
    mode: MonthViewMode,
    canGoPrevious: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val modeText = when (mode) {
        MonthViewMode.PAST -> "지난 달 결산"
        MonthViewMode.CURRENT -> "이번 달 진행 중"
        MonthViewMode.FUTURE -> "다가올 달 계획"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                enabled = canGoPrevious
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    tint = if (canGoPrevious) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = periodText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            AssistChip(
                onClick = {},
                label = {
                    Text(modeText)
                }
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    uiState: MonthlyUiState,
    onEditBudgetClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val remainingTitle = when (uiState.mode) {
        MonthViewMode.PAST -> "최종 남은 금액"
        MonthViewMode.CURRENT -> "남은 금액"
        MonthViewMode.FUTURE -> "예상 남은 금액"
    }

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
                    text = "예산 요약",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (expanded) "접기" else "자세히",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            // 핵심 숫자 (모드별 라벨)
            Text(
                text = remainingTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = uiState.remainingAmount.toMoneyText(),
                style = MaterialTheme.typography.headlineSmall
            )

            if (expanded) {
                HorizontalDivider()

                SummaryRow(
                    title = "월 예산",
                    amountText = uiState.monthlyBudget.toMoneyText()
                )

                if (uiState.extraIncomeAmount > 0L) {
                    SummaryRow(
                        title = "추가 수익",
                        amountText = "+${uiState.extraIncomeAmount.toMoneyText()}"
                    )
                }

                SummaryRow(
                    title = "사용 가능 예산",
                    amountText = uiState.totalAvailableBudget.toMoneyText()
                )

                if (uiState.scheduledSavingAmount > 0L) {
                    SummaryRow(
                        title = "저축",
                        amountText = "-${uiState.scheduledSavingAmount.toMoneyText()}"
                    )
                }

                if (uiState.fixedExpenseAmount > 0L) {
                    SummaryRow(
                        title = "고정비",
                        amountText = "-${uiState.fixedExpenseAmount.toMoneyText()}"
                    )
                }

                if (uiState.totalExpense > 0L) {
                    SummaryRow(
                        title = "총 지출",
                        amountText = "-${uiState.totalExpense.toMoneyText()}"
                    )
                }
            }

            OutlinedButton(
                onClick = onEditBudgetClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("이 달 예산 수정")
            }
        }
    }
}

@Composable
private fun SummaryRow(
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
private fun MonthlyScheduledDeductionCard(
    items: List<ScheduledDeductionSummaryUiModel>,
    onItemClick: (Long) -> Unit
) {
    if (items.isEmpty()) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "저축 / 고정비",
                style = MaterialTheme.typography.titleMedium
            )

            items.forEach { item ->
                val typeText = when (item.type) {
                    ScheduledDeductionType.SAVING -> "저축"
                    ScheduledDeductionType.FIXED -> "고정비"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item.id) },
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
        }
    }
}

@Composable
private fun MonthCalendarCard(
    weeks: List<List<MonthlyDayCellUiModel?>>,
    onDateClick: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { cell ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (cell != null) {
                                DayCell(
                                    cell = cell,
                                    onClick = { onDateClick(cell.date) }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendDot(
                    color = MaterialTheme.colorScheme.primary,
                    label = "지출"
                )
                LegendDot(
                    color = MaterialTheme.colorScheme.tertiary,
                    label = "예정 차감"
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = DayDoneAccent.noSpendCheck,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "무지출",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportEntryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📊", style = MaterialTheme.typography.titleMedium)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayCell(
    cell: MonthlyDayCellUiModel,
    onClick: () -> Unit
) {
    val containerColor = when {
        cell.isSelected -> MaterialTheme.colorScheme.primaryContainer
        cell.isToday -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = cell.dayText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            if (cell.isNoSpendSuccess) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "무지출 성공",
                    tint = DayDoneAccent.noSpendCheck,
                    modifier = Modifier.size(11.dp)
                )
            }
            if (cell.hasExpense) {
                Dot(color = MaterialTheme.colorScheme.primary)
            }
            if (cell.hasScheduledDeduction) {
                Dot(color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
private fun LegendDot(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(color = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectedDateCard(
    title: String,
    expenses: List<TodayExpenseUiModel>,
    extraIncomes: List<TodayExtraIncomeUiModel>,
    scheduledDeductions: List<ScheduledDeductionSummaryUiModel>,
    onExpenseClick: (Long) -> Unit,
    onExtraIncomeClick: (Long) -> Unit,
    onScheduledDeductionClick: (Long) -> Unit
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
                    text = "이 날은 기록된 내역이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            scheduledDeductions.forEach { item ->
                val typeText = when (item.type) {
                    ScheduledDeductionType.SAVING -> "저축"
                    ScheduledDeductionType.FIXED -> "고정비"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScheduledDeductionClick(item.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.title} 예정 · $typeText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "-${item.amount.toMoneyText()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            extraIncomes.forEach { income ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExtraIncomeClick(income.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${income.title} (수익)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "+${income.amount.toMoneyText()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            expenses.forEach { expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpenseClick(expense.id) },
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetInputBottomSheet(
    monthTitle: String,
    budgetInput: String,
    onBudgetInputChange: (String) -> Unit,
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
                text = "$monthTitle 예산 수정",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "이 달부터 적용될 예산이에요. 다음 달은 값을 바꾸기 전까지 이 금액을 이어갑니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = budgetInput,
                onValueChange = onBudgetInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("월 예산")
                },
                singleLine = true,
                suffix = {
                    Text("원")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                supportingText = {
                    val amount = budgetInput.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        Text("설정될 예산: ${amount.toMoneyText()}")
                    }
                }
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = (budgetInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text("저장")
            }
        }
    }
}
