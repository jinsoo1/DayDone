package com.jsworld.android.daydone.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.PurchaseImpact
import com.jsworld.android.daydone.presentation.today.model.PurchaseEvaluationUiModel
import com.jsworld.android.daydone.presentation.util.toMoneyText

/**
 * 살까 말까 시트 — 입력 → 결과 → (보류 시) 확인, 화면 이동 없이 한 시트에서 끝낸다.
 * 앱은 사라/사지 마라를 말하지 않는다. 숫자가 스스로 말하게 하고, 결정은 유저가 한다. (§13)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PurchaseDecisionSheet(
    titleInput: String,
    amountInput: String,
    result: PurchaseEvaluationUiModel?,
    heldDone: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onEvaluateClick: () -> Unit,
    onBuyClick: () -> Unit,
    onHoldClick: () -> Unit,
    onPrepareInVaultClick: () -> Unit,
    onDismiss: () -> Unit
) {
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
            when {
                heldDone -> PurchaseHeldDoneContent(onConfirm = onDismiss)

                result == null -> PurchaseInputContent(
                    titleInput = titleInput,
                    amountInput = amountInput,
                    onTitleChange = onTitleChange,
                    onAmountChange = onAmountChange,
                    onEvaluateClick = onEvaluateClick
                )

                else -> {
                    PurchaseEvaluationSummary(result = result)

                    if (result.impact == PurchaseImpact.IMPOSSIBLE) {
                        Button(
                            onClick = onPrepareInVaultClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.purchase_geumgoe_junbihagi))
                        }
                        OutlinedButton(
                            onClick = onBuyClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.purchase_geuraedo_salgeyo))
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onHoldClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.purchase_boryuhalgeyo))
                            }
                            FilledTonalButton(
                                onClick = onBuyClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.purchase_salgeyo))
                            }
                        }
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
    }
}

@Composable
private fun PurchaseInputContent(
    titleInput: String,
    amountInput: String,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onEvaluateClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.purchase_salkka_malkka),
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        text = stringResource(R.string.purchase_jigeum_samyeon_haru_gweonjang),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = titleInput,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.purchase_pummogmyeong)) },
        placeholder = { Text(stringResource(R.string.purchase_ye_museon_ieopon)) },
        singleLine = true
    )

    OutlinedTextField(
        value = amountInput,
        onValueChange = onAmountChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.purchase_gagyeog)) },
        singleLine = true,
        suffix = { Text(stringResource(R.string.onboarding_weon)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            val amount = amountInput.toLongOrNull() ?: 0L
            if (amount > 0L) {
                Text(stringResource(R.string.purchase_ibryeoghan_gagyeog, amount.toMoneyText()))
            }
        }
    )

    Button(
        onClick = onEvaluateClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = titleInput.isNotBlank() && (amountInput.toLongOrNull() ?: 0L) > 0L
    ) {
        Text(stringResource(R.string.purchase_eolmana_dalrajineunji_bogi))
    }
}

/**
 * 결과 요약 — 헤드라인 + 하루 권장 비교 + 남은 생활비 보조 줄.
 * 보류함 화면의 재계산 시트에서도 그대로 재사용한다.
 */
@Composable
internal fun PurchaseEvaluationSummary(result: PurchaseEvaluationUiModel) {
    val headline = when (result.impact) {
        PurchaseImpact.NEGLIGIBLE ->
            stringResource(R.string.purchase_i_jeongdoneun_tido_jal)

        PurchaseImpact.COMFORTABLE ->
            stringResource(R.string.purchase_sado_gwaenchanhayo_nameun_yesan)

        PurchaseImpact.TIGHT ->
            stringResource(R.string.purchase_sal_suneun_isseoyo_daman, result.remainingDays)

        PurchaseImpact.IMPOSSIBLE ->
            if (result.budgetLeft <= 0L) {
                stringResource(R.string.purchase_ibeon_gigan_saenghwalbiga_imi)
            } else {
                stringResource(R.string.purchase_ibeon_gigan_saenghwalbironeun_eoryeoweoyo)
            }
    }

    Text(
        text = "${result.title} · ${result.price.toMoneyText()}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
        text = headline,
        style = MaterialTheme.typography.titleLarge
    )

    if (result.impact == PurchaseImpact.IMPOSSIBLE) {
        Text(
            text = stringResource(R.string.purchase_jigeum_nameun_saenghwalbineun_ieyo, result.budgetLeft.coerceAtLeast(0L).toMoneyText()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.purchase_oneul_gijun_haru_gweonjang, result.remainingDays),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = result.currentDaily.toMoneyText(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.purchase_samyeon, result.afterDaily.toMoneyText()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.purchase_nameun_saenghwalbi, result.budgetLeft.toMoneyText(), result.budgetLeftAfter.toMoneyText()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PurchaseHeldDoneContent(onConfirm: () -> Unit) {
    Text(
        text = stringResource(R.string.purchase_boryuhame_neoheodweosseoyo),
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        text = stringResource(R.string.purchase_30il_dwiedo_saenggagnamyeon_geuttae),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Button(
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.purchase_hwagin))
    }
}
