package com.jsworld.android.daydone.presentation.held

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.R
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
            DayDoneTopBar(title = stringResource(R.string.held_sobi_boryuham), onBack = onBack)

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
                            text = stringResource(R.string.held_ajig_boryuhan_mulgeoni_eobseoyo),
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
                                    text = stringResource(R.string.held_boryu_jung),
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
                                    text = stringResource(R.string.held_jinan_girog),
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
                                    text = stringResource(R.string.held_girogeul_taebhamyeon_sagjehal_su),
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
                text = stringResource(R.string.held_boryuhameuro_akkin_don),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = savedTotal.toMoneyText(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (savedTotal > 0L) {
                    stringResource(R.string.held_an_sagil_jalhan_geosdeuli)
                } else {
                    stringResource(R.string.held_an_sagiro_han_mulgeonui)
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
                text = stringResource(R.string.held_30ili_jinasseoyo_ajigdo_pilyohamyeon),
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
                    Text(stringResource(R.string.held_geuraedo_sasseoyo))
                }
                FilledTonalButton(
                    onClick = onKeepSavedClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.held_akkin_doneuro))
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
                text = stringResource(R.string.held_d_iljjae_boryu_jung, item.daysHeld, item.daysLeft),
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
                text = stringResource(R.string.held_taebhamyeon_oneul_gijuneuro_dasi),
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
                    Text(stringResource(R.string.held_jigeum_salgeyo))
                }
                FilledTonalButton(
                    onClick = onPassClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.held_an_salraeyo))
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
                    Text(stringResource(R.string.held_jigeum_salgeyo))
                }
                FilledTonalButton(
                    onClick = { onPassClick(id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.held_an_salraeyo))
                }
            }

            TextButton(
                onClick = { onDeleteClick(id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.held_boryuhameseo_sagjehagi),
                    color = MaterialTheme.colorScheme.error
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.purchase_dadgi))
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
        title = { Text(stringResource(R.string.held_eul_reul_sagjehalkkayo, title)) },
        text = {
            Text(
                if (isSaved) {
                    stringResource(R.string.held_i_hangmogeun_akkin_doneuro)
                } else {
                    stringResource(R.string.held_boryuhameseo_i_hangmogi_sarajigo)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.held_sagje), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.held_chwiso))
            }
        }
    )
}
