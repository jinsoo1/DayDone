package com.jsworld.android.daydone.presentation.held

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.presentation.today.ExpenseInputBottomSheet
import com.jsworld.android.daydone.presentation.today.PurchaseEvaluationSummary
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.theme.DayDoneAccent

@Composable
fun HeldPurchasesRoute(
    onBack: () -> Unit,
    viewModel: HeldPurchasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    HeldPurchasesScreen(
        uiState = uiState,
        onBack = onBack,
        onItemClick = viewModel::onItemClick,
        onBuyNowClick = viewModel::onBuyNowClick,
        onPassClick = viewModel::onPassClick,
        onBoughtAnywayClick = viewModel::onBoughtAnywayClick,
        onKeepSavedClick = viewModel::onKeepSavedClick,
        onDeleteClick = viewModel::onDeleteClick
    )

    if (uiState.evaluation != null) {
        HeldEvaluationSheet(
            uiState = uiState,
            onBuyNowClick = viewModel::onBuyNowClick,
            onPassClick = viewModel::onPassClick,
            onDeleteClick = viewModel::onDeleteClick,
            onDismiss = viewModel::onEvaluationDismiss
        )
    }

    if (uiState.deleteConfirmId != null) {
        DeleteConfirmDialog(
            title = uiState.deleteConfirmTitle,
            isSaved = uiState.deleteConfirmIsSaved,
            onConfirm = viewModel::onDeleteConfirm,
            onDismiss = viewModel::onDeleteDismiss
        )
    }

    if (uiState.isExpenseSheetVisible) {
        ExpenseInputBottomSheet(
            isEditing = false,
            titleInput = uiState.expenseTitleInput,
            amountInput = uiState.expenseAmountInput,
            dateInput = uiState.expenseDateInput,
            onTitleChange = viewModel::onExpenseTitleChange,
            onAmountChange = viewModel::onExpenseAmountChange,
            onDateChange = viewModel::onExpenseDateChange,
            onDismiss = viewModel::onExpenseSheetDismiss,
            onAddClick = viewModel::onExpenseSaveClick,
            onDeleteClick = {}
        )
    }
}

@Composable
fun HeldPurchasesScreen(
    uiState: HeldPurchasesUiState,
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    onBuyNowClick: (Long) -> Unit,
    onPassClick: (Long) -> Unit,
    onBoughtAnywayClick: (Long) -> Unit,
    onKeepSavedClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit = {}
) {
    val isEmpty = uiState.dueItems.isEmpty() &&
            uiState.holdingItems.isEmpty() &&
            uiState.records.isEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(title = "소비 보류함", onBack = onBack)

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isEmpty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 보류한 물건이 없어요.\n" +
                                    "살까 말까 고민되는 물건은 하단 '+' → '살까 말까?'에서 " +
                                    "잠깐 보류해보세요. 30일 뒤에도 생각나면 그때 사요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 8.dp,
                            end = 20.dp,
                            bottom = 40.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SavedTotalCard(savedTotal = uiState.savedTotal)
                        }

                        items(uiState.dueItems, key = { "due-${it.id}" }) { item ->
                            DueItemCard(
                                item = item,
                                onBoughtAnywayClick = { onBoughtAnywayClick(item.id) },
                                onKeepSavedClick = { onKeepSavedClick(item.id) }
                            )
                        }

                        if (uiState.holdingItems.isNotEmpty()) {
                            item {
                                Text(
                                    text = "보류 중",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(uiState.holdingItems, key = { "hold-${it.id}" }) { item ->
                                HoldingItemCard(
                                    item = item,
                                    onClick = { onItemClick(item.id) },
                                    onBuyNowClick = { onBuyNowClick(item.id) },
                                    onPassClick = { onPassClick(item.id) }
                                )
                            }
                        }

                        if (uiState.records.isNotEmpty()) {
                            item {
                                Text(
                                    text = "지난 기록",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(uiState.records, key = { "rec-${it.id}" }) { record ->
                                RecordRow(
                                    record = record,
                                    onClick = { onDeleteClick(record.id) }
                                )
                            }
                            item {
                                Text(
                                    text = "기록을 탭하면 삭제할 수 있어요.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedTotalCard(savedTotal: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DayDoneAccent.successContainer,
            contentColor = DayDoneAccent.onSuccessContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "보류함으로 아낀 돈",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = savedTotal.toMoneyText(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (savedTotal > 0L) {
                    "안 사길 잘한 것들이 쌓이고 있어요."
                } else {
                    "안 사기로 한 물건의 금액이 여기에 쌓여요."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** 30일이 지나 자동으로 아낀 돈이 된 항목 — 딱 한 번 묻는 카드. */
@Composable
private fun DueItemCard(
    item: HeldDueUiModel,
    onBoughtAnywayClick: () -> Unit,
    onKeepSavedClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.amount.toMoneyText(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "30일이 지났어요. 아직도 필요하면 그때 사요 — 일단 아낀 돈에 넣어뒀어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBoughtAnywayClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("그래도 샀어요")
                }
                FilledTonalButton(
                    onClick = onKeepSavedClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("아낀 돈으로")
                }
            }
        }
    }
}

@Composable
private fun HoldingItemCard(
    item: HeldHoldingUiModel,
    onClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onPassClick: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.amount.toMoneyText(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "D+${item.daysHeld}일째 보류 중 · ${item.daysLeft}일 뒤 아낀 돈이 돼요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            Text(
                text = "탭하면 오늘 기준으로 다시 계산해드려요",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onBuyNowClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("지금 살게요")
                }
                FilledTonalButton(
                    onClick = onPassClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("안 살래요")
                }
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: HeldRecordUiModel,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = record.statusLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (record.isSaved) {
                        DayDoneAccent.successText
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = record.amount.toMoneyText(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (record.isSaved) {
                    DayDoneAccent.successText
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/** 보류 항목 탭 → 오늘 기준 재계산 결과 시트. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeldEvaluationSheet(
    uiState: HeldPurchasesUiState,
    onBuyNowClick: (Long) -> Unit,
    onPassClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val result = uiState.evaluation ?: return
    val id = uiState.evaluatingId ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            PurchaseEvaluationSummary(result = result)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onBuyNowClick(id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("지금 살게요")
                }
                FilledTonalButton(
                    onClick = { onPassClick(id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("안 살래요")
                }
            }

            TextButton(
                onClick = { onDeleteClick(id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "보류함에서 삭제하기",
                    color = MaterialTheme.colorScheme.error
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("닫기")
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    isSaved: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("'${title}'을(를) 삭제할까요?") },
        text = {
            Text(
                if (isSaved) {
                    "이 항목은 아낀 돈으로 집계돼 있어서, 삭제하면 아낀 돈 합계에서도 빠져요. 되돌릴 수 없어요."
                } else {
                    "보류함에서 이 항목이 사라지고 되돌릴 수 없어요."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
