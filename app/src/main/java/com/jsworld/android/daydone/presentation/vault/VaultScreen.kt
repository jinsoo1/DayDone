package com.jsworld.android.daydone.presentation.vault

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jsworld.android.daydone.R
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
    onDeleteItemClick: () -> Unit,
    onHeldPurchasesClick: () -> Unit = {}
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
        DayDoneTopBar(title = stringResource(R.string.vault_geumgo))

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
                        text = stringResource(R.string.vault_junbi_jungin_hangmog),
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
                        text = stringResource(R.string.vault_nabbu_wanryo),
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
                    Text(stringResource(R.string.vault_junbi_hangmog_chuga))
                }
            }

            item {
                HeldPurchasesCard(
                    savedTotal = uiState.heldSavedTotal,
                    holdingCount = uiState.heldHoldingCount,
                    dueBadge = uiState.heldDueBadge,
                    onClick = onHeldPurchasesClick
                )
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

/** 소비 보류함 진입 카드 — 금고=모아둔 돈, 보류함=아낀 돈. */
@Composable
private fun HeldPurchasesCard(
    savedTotal: Long,
    holdingCount: Int,
    dueBadge: Boolean,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.vault_sobi_boryuham),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (dueBadge) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Text(
                    text = if (savedTotal > 0L || holdingCount > 0) {
                        stringResource(R.string.vault_akkin_don_boryu_jung, savedTotal.toMoneyText(), holdingCount)
                    } else {
                        stringResource(R.string.vault_gomindoeneun_mulgeoneun_salkka_malkka)
                    },
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
        title = { Text(stringResource(R.string.vault_ttaro_olmgyeodugi, title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.vault_ibeon_dal_chucheon_saenghwalbi, monthlyTarget.toMoneyText()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.vault_olmgin_geumaeg)) },
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.settings_weon)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = (amountInput.toLongOrNull() ?: 0L) > 0L
            ) {
                Text(stringResource(R.string.vault_olmgyeodweosseoyo))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_chwiso)) }
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
                text = stringResource(R.string.vault_miri_junbihaneun_geumgo),
                style = MaterialTheme.typography.titleMedium
            )

            if (isEmpty) {
                Text(
                    text = stringResource(R.string.vault_aebi_doneul_bogwanhajin_anhayo),
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
                        text = stringResource(R.string.vault_yereul_deuleo_6weole_nael),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.vault_6weole_han_beone_naemyeon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GuideStep("1", stringResource(R.string.vault_junbi_hangmogeul_chugahaeyo), stringResource(R.string.vault_ye_jadongchase_40man_weon))
                GuideStep("2", stringResource(R.string.vault_maedal_chucheon_geumaegmankeum_ttaro), stringResource(R.string.vault_olmgin_dwi_olmgyeodweosseoyo_reul))
                GuideStep("3", stringResource(R.string.vault_mogpyohan_dale_nabbu_wanryo), stringResource(R.string.vault_miri_olmgyeodun_doneuro_gyeolje))

                Text(
                    text = stringResource(R.string.vault_saenghwalbi_tongjanggwa_seokkiji_anhge),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = stringResource(R.string.vault_ttaro_olmgyeodun_mankeumeun_imi),
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
                    text = stringResource(R.string.vault_ibeon_dal_olmgyeodugi_jean),
                    style = MaterialTheme.typography.labelLarge,
                    color = onContainer.copy(alpha = 0.75f)
                )
                if (allDone) {
                    Text(
                        text = stringResource(R.string.vault_ibeon_dal_olmgil_mankeum),
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
                        text = stringResource(R.string.vault_ttaro_tongjange_olmgyeodumyeon_johayo),
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
                    text = stringResource(R.string.vault_ibeon_dal_olmgim, item.thisMonthPrepared.toMoneyText()),
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = stringResource(R.string.vault_chucheon_olmgim, item.monthlyTarget.toMoneyText(), item.thisMonthPrepared.toMoneyText()),
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
                text = stringResource(R.string.vault_deo_olmgigi),
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
                    text = stringResource(R.string.vault_olmgyeodugi),
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
                    text = stringResource(R.string.vault_junbidoem, item.preparedAmount.toMoneyText()),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.vault_nameun, item.remainingAmount.toMoneyText()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.vault_mogpyo, item.totalAmount.toMoneyText()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.vault_taebhayeo_gwanri_nabbu),
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
            Text(stringResource(R.string.vault_nabbu_wanryo_2, item.title), style = MaterialTheme.typography.bodyMedium)
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
                    title = if (isEditing) stringResource(R.string.vault_junbi_hangmog_sujeong) else stringResource(R.string.vault_junbi_hangmog_chuga_2),
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
                        label = { Text(stringResource(R.string.vault_hangmogmyeong)) },
                        placeholder = { Text(stringResource(R.string.vault_ye_jadongchase_eomma_saengsin)) },
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
                        label = { Text(stringResource(R.string.vault_mogpyo_geumaeg)) },
                        singleLine = true,
                        suffix = { Text(stringResource(R.string.settings_weon)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    MonthSelectorField(
                        label = stringResource(R.string.vault_mogpyoweol_nabbu_ibenteu),
                        yearMonth = uiState.targetMonthInput,
                        onChange = onTargetMonthChange
                    )

                    MonthSelectorField(
                        label = stringResource(R.string.vault_junbi_sijagweol),
                        yearMonth = uiState.prepareStartMonthInput,
                        onChange = onPrepareStartMonthChange
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.repeatInput == FutureExpenseRepeat.ONCE,
                            onClick = { onRepeatChange(FutureExpenseRepeat.ONCE) },
                            label = { Text(stringResource(R.string.vault_1hoe)) }
                        )
                        FilterChip(
                            selected = uiState.repeatInput == FutureExpenseRepeat.YEARLY,
                            onClick = { onRepeatChange(FutureExpenseRepeat.YEARLY) },
                            label = { Text(stringResource(R.string.vault_maenyeon)) }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.memoInput,
                        onValueChange = onMemoChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.vault_memo)) },
                        placeholder = { Text(stringResource(R.string.vault_seontaeg_ibryeog)) },
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
                        Text(if (isEditing) stringResource(R.string.vault_sujeonghagi) else stringResource(R.string.vault_chugahagi))
                    }

                    if (isEditing) {
                        if (uiState.editingPreparedAmount > 0L && !uiState.editingIsCompleted) {
                            Text(
                                text = stringResource(R.string.vault_ibeon_saikeul_junbiham, uiState.editingPreparedAmount.toMoneyText()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!uiState.editingIsCompleted) {
                            OutlinedButton(
                                onClick = { showPaymentConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.vault_nabbu_wanryo))
                            }
                        }

                        if (!uiState.editingIsCompleted && uiState.editingPreparedAmount > 0L) {
                            OutlinedButton(
                                onClick = onWithdrawClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.vault_junbigeum_ppaegi))
                            }
                        }

                        if (uiState.editingCanUndo) {
                            OutlinedButton(
                                onClick = { showUndoConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.vault_nabbu_wanryo_chwiso))
                            }
                        }

                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.vault_sagjehagi), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

    if (showPaymentConfirm) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirm = false },
            title = { Text(stringResource(R.string.vault_nabbu_wanryoro_cheorihalkkayo)) },
            text = {
                Text(stringResource(R.string.vault_junbihan_mankeumeun_imi_saenghwalbieseo))
            },
            confirmButton = {
                TextButton(onClick = {
                    showPaymentConfirm = false
                    onCompletePaymentClick()
                }) {
                    Text(stringResource(R.string.vault_nabbu_wanryo))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentConfirm = false }) {
                    Text(stringResource(R.string.settings_chwiso))
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.vault_sagjehalkkayo)) },
            text = {
                Text(stringResource(R.string.vault_i_junbi_hangmoggwa_geudongan))
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) {
                    Text(stringResource(R.string.vault_sagje), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.settings_chwiso))
                }
            }
        )
    }

    if (showUndoConfirm) {
        AlertDialog(
            onDismissRequest = { showUndoConfirm = false },
            title = { Text(stringResource(R.string.vault_nabbu_wanryoreul_chwisohalkkayo)) },
            text = {
                Text(stringResource(R.string.vault_i_hangmogeul_dasi_junbi))
            },
            confirmButton = {
                TextButton(onClick = {
                    showUndoConfirm = false
                    onUndoPaymentClick()
                }) {
                    Text(stringResource(R.string.vault_nabbu_wanryo_chwiso))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUndoConfirm = false }) {
                    Text(stringResource(R.string.vault_dadgi))
                }
            }
        )
    }

    if (uiState.isWithdrawDialogVisible) {
        AlertDialog(
            onDismissRequest = onWithdrawDismiss,
            title = { Text(stringResource(R.string.vault_junbigeum_ppaegi)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.vault_choedae_kkaji_ppael_su, uiState.withdrawMax.toMoneyText()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = uiState.withdrawAmountInput,
                        onValueChange = onWithdrawAmountChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.vault_ppaeneun_geumaeg)) },
                        singleLine = true,
                        suffix = { Text(stringResource(R.string.settings_weon)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onWithdrawConfirm,
                    enabled = (uiState.withdrawAmountInput.toLongOrNull() ?: 0L) > 0L
                ) {
                    Text(stringResource(R.string.vault_ppaegi))
                }
            },
            dismissButton = {
                TextButton(onClick = onWithdrawDismiss) {
                    Text(stringResource(R.string.settings_chwiso))
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
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.vault_ijeon_dal))
            }
            Text(
                text = stringResource(R.string.vault_nyeon_weol, yearMonth.year, yearMonth.monthValue),
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = { onChange(yearMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.vault_daeum_dal))
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun FutureExpenseCategory.label(): String = when (this) {
    FutureExpenseCategory.TAX -> stringResource(R.string.vault_segeum)
    FutureExpenseCategory.INSURANCE -> stringResource(R.string.vault_boheom)
    FutureExpenseCategory.ANNIVERSARY -> stringResource(R.string.vault_ginyeomil)
    FutureExpenseCategory.ETC -> stringResource(R.string.vault_gita)
}

@Composable
private fun VaultItemUiModel.categoryLabel(): String = category.label()
