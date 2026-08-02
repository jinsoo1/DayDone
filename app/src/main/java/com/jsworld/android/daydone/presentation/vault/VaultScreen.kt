package com.jsworld.android.daydone.presentation.vault

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.jsworld.android.daydone.domain.model.FutureExpenseCategory
import com.jsworld.android.daydone.domain.model.FutureExpenseRepeat
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.presentation.vault.model.VaultItemUiModel
import com.jsworld.android.daydone.presentation.vault.model.VaultSuggestionUiModel
import com.jsworld.android.daydone.presentation.vault.model.VaultUiState
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import java.time.YearMonth

@Composable
fun VaultScreen(
    uiState: VaultUiState,
    onFullScreenChange: (Boolean) -> Unit = {},
    onPrepareClick: (Long) -> Unit,
    onPrepareAmountChange: (String) -> Unit,
    onPrepareConfirm: () -> Unit,
    onPrepareDialogDismiss: () -> Unit,
    onCompletePaymentClick: () -> Unit,
    onUndoPaymentClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onWithdrawAmountChange: (String) -> Unit,
    onWithdrawConfirm: () -> Unit,
    onWithdrawDismiss: () -> Unit,
    onAddItemClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onInputDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (FutureExpenseCategory) -> Unit,
    onTotalAmountChange: (String) -> Unit,
    onTargetMonthChange: (YearMonth) -> Unit,
    onPrepareStartMonthChange: (YearMonth) -> Unit,
    onRepeatChange: (FutureExpenseRepeat) -> Unit,
    onMemoChange: (String) -> Unit,
    onSaveItemClick: () -> Unit,
    onDeleteItemClick: () -> Unit
) {
    BackHandler(enabled = uiState.isInputSheetVisible) { onInputDismiss() }

    LaunchedEffect(uiState.isInputSheetVisible) {
        onFullScreenChange(uiState.isInputSheetVisible)
    }

    // 금고 탭을 벗어날 때 하단바 복구 (안전장치)
    DisposableEffect(Unit) {
        onDispose { onFullScreenChange(false) }
    }

    if (uiState.isInputSheetVisible) {
        FutureExpenseInputSheet(
            uiState = uiState,
            onTitleChange = onTitleChange,
            onCategoryChange = onCategoryChange,
            onTotalAmountChange = onTotalAmountChange,
            onTargetMonthChange = onTargetMonthChange,
            onPrepareStartMonthChange = onPrepareStartMonthChange,
            onRepeatChange = onRepeatChange,
            onMemoChange = onMemoChange,
            onSaveClick = onSaveItemClick,
            onCompletePaymentClick = onCompletePaymentClick,
            onUndoPaymentClick = onUndoPaymentClick,
            onWithdrawClick = onWithdrawClick,
            onWithdrawAmountChange = onWithdrawAmountChange,
            onWithdrawConfirm = onWithdrawConfirm,
            onWithdrawDismiss = onWithdrawDismiss,
            onDeleteClick = onDeleteItemClick,
            onDismiss = onInputDismiss
        )
        return
    }

    val isEmpty = uiState.items.isEmpty() && uiState.completedItems.isEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        DayDoneTopBar(title = "금고")

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GuideCard(isEmpty = isEmpty) }

            if (uiState.suggestions.isNotEmpty()) {
                item {
                    SuggestionCard(
                        total = uiState.suggestionTotal,
                        suggestions = uiState.suggestions,
                        onPrepareClick = onPrepareClick
                    )
                }
            }

            if (uiState.items.isNotEmpty()) {
                item {
                    Text(
                        text = "준비 중인 항목",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.items.size) { i ->
                    val item = uiState.items[i]
                    VaultItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }

            if (uiState.completedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "납부 완료",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.completedItems.size) { i ->
                    val item = uiState.completedItems[i]
                    CompletedItemRow(item = item, onClick = { onItemClick(item.id) })
                }
            }

            item {
                OutlinedButton(
                    onClick = onAddItemClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ 준비 항목 추가")
                }
            }
        }
    }

    if (uiState.isPrepareDialogVisible) {
        PrepareAmountDialog(
            title = uiState.prepareTargetTitle,
            monthlyTarget = uiState.prepareMonthlyTarget,
            amountInput = uiState.prepareAmountInput,
            onAmountChange = onPrepareAmountChange,
            onConfirm = onPrepareConfirm,
            onDismiss = onPrepareDialogDismiss
        )
    }
}

@Composable
private fun PrepareAmountDialog(
    title: String,
    monthlyTarget: Long,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title · 따로 옮겨두기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "이번 달 추천 ${monthlyTarget.toMoneyText()}. 생활비 통장 말고 따로 모으는 통장에 옮긴 뒤, 옮긴 금액을 적어주세요. 그만큼 오늘 생활비에서 빠져요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("옮긴 금액") },
                    singleLine = true,
                    suffix = { Text("원") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text("옮겨뒀어요")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@Composable
private fun GuideCard(isEmpty: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "미리 준비하는 금고",
                style = MaterialTheme.typography.titleMedium
            )

            if (isEmpty) {
                Text(
                    text = "앱이 돈을 보관하진 않아요. 큰 지출이 오기 전에, 언제부터 얼마씩 따로 옮겨두면 좋을지 알려드릴게요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 구체 예시 (개념 이해용)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "예를 들어, 6월에 낼 자동차세 40만 원",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "6월에 한 번에 내면 그 달이 휘청여요.\n3월부터 매달 10만 원씩 따로 통장에 옮겨두면, 6월엔 이미 준비 끝이에요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GuideStep("1", "준비 항목을 추가해요", "예: 자동차세 40만 원 · 6월 납부")
                GuideStep("2", "매달 추천 금액만큼 따로 통장에 옮겨요", "옮긴 뒤 '옮겨뒀어요'를 누르면 그만큼 생활비에서 빠져요")
                GuideStep("3", "목표한 달에 '납부 완료'", "미리 옮겨둔 돈으로 결제, 그 달엔 다시 안 빠져요")

                Text(
                    text = "생활비 통장과 섞이지 않게 따로 모으는 통장에 두면 더 확실해요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "따로 옮겨둔 만큼은 이미 생활비에서 빠졌어요. 목표한 달엔 그 돈으로 결제하면 돼요. 매달 추천 금액만큼 계속 옮겨두세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuggestionCard(
    total: Long,
    suggestions: List<VaultSuggestionUiModel>,
    onPrepareClick: (Long) -> Unit
) {
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val allDone = total <= 0L

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = onContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 헤더: 작은 라벨 + 큰 금액
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "이번 달 옮겨두기 제안",
                    style = MaterialTheme.typography.labelLarge,
                    color = onContainer.copy(alpha = 0.75f)
                )
                if (allDone) {
                    Text(
                        text = "이번 달 옮길 만큼 다 옮겼어요 ✓",
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        text = buildString {
                            append(total.toMoneyText())
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "따로 통장에 옮겨두면 좋아요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer.copy(alpha = 0.75f)
                    )
                }
            }

            HorizontalDivider(color = onContainer.copy(alpha = 0.18f))

            suggestions.forEach { s ->
                SuggestionRow(
                    item = s,
                    onContainer = onContainer,
                    onPrepareClick = { onPrepareClick(s.id) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    item: VaultSuggestionUiModel,
    onContainer: androidx.compose.ui.graphics.Color,
    onPrepareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.title + " · " + item.targetMonthLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (item.isDone) {
                Text(
                    text = "✓ 이번 달 ${item.thisMonthPrepared.toMoneyText()} 옮김",
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "추천 ${item.monthlyTarget.toMoneyText()} · 옮김 ${item.thisMonthPrepared.toMoneyText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = 0.7f)
                )

                val fraction = if (item.monthlyTarget > 0L) {
                    (item.thisMonthPrepared.toFloat() / item.monthlyTarget.toFloat())
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = onContainer,
                    trackColor = onContainer.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }

        if (item.isDone) {
            Text(
                text = "더 옮기기",
                style = MaterialTheme.typography.labelLarge,
                color = onContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onPrepareClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        } else {
            Surface(
                onClick = onPrepareClick,
                shape = CircleShape,
                color = onContainer.copy(alpha = 0.12f),
                contentColor = onContainer
            ) {
                Text(
                    text = "옮겨두기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItemUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${item.categoryLabel()} · ${item.targetMonthLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "준비됨 ${item.preparedAmount.toMoneyText()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "남은 ${item.remainingAmount.toMoneyText()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "목표 ${item.totalAmount.toMoneyText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "탭하여 관리 · 납부",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompletedItemRow(
    item: VaultItemUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${item.title} · 납부 완료", style = MaterialTheme.typography.bodyMedium)
            Text(item.totalAmount.toMoneyText(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FutureExpenseInputSheet(
    uiState: VaultUiState,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (FutureExpenseCategory) -> Unit,
    onTotalAmountChange: (String) -> Unit,
    onTargetMonthChange: (YearMonth) -> Unit,
    onPrepareStartMonthChange: (YearMonth) -> Unit,
    onRepeatChange: (FutureExpenseRepeat) -> Unit,
    onMemoChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCompletePaymentClick: () -> Unit,
    onUndoPaymentClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onWithdrawAmountChange: (String) -> Unit,
    onWithdrawConfirm: () -> Unit,
    onWithdrawDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = uiState.editingId != null
    var showPaymentConfirm by remember { mutableStateOf(false) }
    var showUndoConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(
                    title = if (isEditing) "준비 항목 수정" else "준비 항목 추가",
                    onBack = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.titleInput,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("항목명") },
                        placeholder = { Text("예: 자동차세, 엄마 생신 선물") },
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FutureExpenseCategory.entries.forEach { category ->
                            FilterChip(
                                selected = uiState.categoryInput == category,
                                onClick = { onCategoryChange(category) },
                                label = { Text(category.label()) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.totalAmountInput,
                        onValueChange = onTotalAmountChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("목표 금액") },
                        singleLine = true,
                        suffix = { Text("원") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    MonthSelectorField(
                        label = "목표월 (납부/이벤트)",
                        yearMonth = uiState.targetMonthInput,
                        onChange = onTargetMonthChange
                    )

                    MonthSelectorField(
                        label = "준비 시작월",
                        yearMonth = uiState.prepareStartMonthInput,
                        onChange = onPrepareStartMonthChange
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.repeatInput == FutureExpenseRepeat.ONCE,
                            onClick = { onRepeatChange(FutureExpenseRepeat.ONCE) },
                            label = { Text("1회") }
                        )
                        FilterChip(
                            selected = uiState.repeatInput == FutureExpenseRepeat.YEARLY,
                            onClick = { onRepeatChange(FutureExpenseRepeat.YEARLY) },
                            label = { Text("매년") }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.memoInput,
                        onValueChange = onMemoChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("메모") },
                        placeholder = { Text("선택 입력") },
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.titleInput.isNotBlank() &&
                                (uiState.totalAmountInput.toLongOrNull() ?: 0L) > 0L
                    ) {
                        Text(if (isEditing) "수정하기" else "추가하기")
                    }

                    if (isEditing) {
                        if (uiState.editingPreparedAmount > 0L && !uiState.editingIsCompleted) {
                            Text(
                                text = "이번 사이클 준비함 ${uiState.editingPreparedAmount.toMoneyText()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!uiState.editingIsCompleted) {
                            OutlinedButton(
                                onClick = { showPaymentConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("납부 완료")
                            }
                        }

                        if (!uiState.editingIsCompleted && uiState.editingPreparedAmount > 0L) {
                            OutlinedButton(
                                onClick = onWithdrawClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("준비금 빼기")
                            }
                        }

                        if (uiState.editingCanUndo) {
                            OutlinedButton(
                                onClick = { showUndoConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("납부 완료 취소")
                            }
                        }

                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("삭제하기", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

    if (showPaymentConfirm) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirm = false },
            title = { Text("납부 완료로 처리할까요?") },
            text = {
                Text("준비한 만큼은 이미 생활비에서 빠졌고, 부족분이 있으면 이번 달 지출로 추가돼요. 반복 항목이면 다음 사이클로 넘어갑니다.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showPaymentConfirm = false
                    onCompletePaymentClick()
                }) {
                    Text("납부 완료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirm = false }) {
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
                Text("이 준비 항목과 그동안 준비한 내역이 함께 삭제되고, 준비금은 생활비로 되돌아와요.")
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

    if (showUndoConfirm) {
        AlertDialog(
            onDismissRequest = { showUndoConfirm = false },
            title = { Text("납부 완료를 취소할까요?") },
            text = {
                Text("이 항목을 다시 '준비 중' 상태로 되돌립니다. 이미 준비해둔 금액은 그대로 남아요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showUndoConfirm = false
                    onUndoPaymentClick()
                }) {
                    Text("납부 완료 취소")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUndoConfirm = false }) {
                    Text("닫기")
                }
            }
        )
    }

    if (uiState.isWithdrawDialogVisible) {
        AlertDialog(
            onDismissRequest = onWithdrawDismiss,
            title = { Text("준비금 빼기") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "최대 ${uiState.withdrawMax.toMoneyText()}까지 뺄 수 있어요. 뺀 만큼 생활비로 돌아옵니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = uiState.withdrawAmountInput,
                        onValueChange = onWithdrawAmountChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("빼는 금액") },
                        singleLine = true,
                        suffix = { Text("원") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onWithdrawConfirm,
                    enabled = (uiState.withdrawAmountInput.toLongOrNull() ?: 0L) > 0L
                ) {
                    Text("빼기")
                }
            },
            dismissButton = {
                TextButton(onClick = onWithdrawDismiss) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
private fun MonthSelectorField(
    label: String,
    yearMonth: YearMonth,
    onChange: (YearMonth) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onChange(yearMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달")
            }
            Text(
                text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = { onChange(yearMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달")
            }
        }
        HorizontalDivider()
    }
}

private fun FutureExpenseCategory.label(): String = when (this) {
    FutureExpenseCategory.TAX -> "세금"
    FutureExpenseCategory.INSURANCE -> "보험"
    FutureExpenseCategory.ANNIVERSARY -> "기념일"
    FutureExpenseCategory.ETC -> "기타"
}

private fun VaultItemUiModel.categoryLabel(): String = category.label()
