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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
                            Text("금고에 준비하기")
                        }
                        OutlinedButton(
                            onClick = onBuyClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("그래도 살게요")
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
                                Text("보류할게요")
                            }
                            FilledTonalButton(
                                onClick = onBuyClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("살게요")
                            }
                        }
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
        text = "살까 말까?",
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        text = "지금 사면 하루 권장 금액이 얼마나 달라지는지 보여드릴게요. 결정은 그다음에 해도 늦지 않아요.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = titleInput,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("품목명") },
        placeholder = { Text("예: 무선 이어폰") },
        singleLine = true
    )

    OutlinedTextField(
        value = amountInput,
        onValueChange = onAmountChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("가격") },
        singleLine = true,
        suffix = { Text("원") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            val amount = amountInput.toLongOrNull() ?: 0L
            if (amount > 0L) {
                Text("입력한 가격: ${amount.toMoneyText()}")
            }
        }
    )

    Button(
        onClick = onEvaluateClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = titleInput.isNotBlank() && (amountInput.toLongOrNull() ?: 0L) > 0L
    ) {
        Text("얼마나 달라지는지 보기")
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
            "이 정도는 티도 잘 안 나요.\n사도 괜찮아요."

        PurchaseImpact.COMFORTABLE ->
            "사도 괜찮아요.\n남은 예산 안에서 소화돼요."

        PurchaseImpact.TIGHT ->
            "살 수는 있어요. 다만 남은 ${result.remainingDays}일이 조금 빠듯해져요.\n잠깐 보류해볼까요?"

        PurchaseImpact.IMPOSSIBLE ->
            if (result.budgetLeft <= 0L) {
                "이번 기간 생활비가 이미 다 쓰였어요.\n금고에서 나눠 준비하면 다음 기간이 편해져요."
            } else {
                "이번 기간 생활비로는 어려워요.\n금고에서 몇 달에 나눠 준비하면 마음 편히 살 수 있어요."
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
            text = "지금 남은 생활비는 ${result.budgetLeft.coerceAtLeast(0L).toMoneyText()}이에요.",
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
            text = "오늘 기준 하루 권장 (남은 ${result.remainingDays}일 동안)",
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
                text = "사면 ${result.afterDaily.toMoneyText()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()

        Text(
            text = "남은 생활비 ${result.budgetLeft.toMoneyText()} → ${result.budgetLeftAfter.toMoneyText()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PurchaseHeldDoneContent(onConfirm: () -> Unit) {
    Text(
        text = "보류함에 넣어뒀어요",
        style = MaterialTheme.typography.titleLarge
    )

    Text(
        text = "30일 뒤에도 생각나면 그때 사요. 안 사면 그만큼 아낀 돈이 돼요.\n금고 탭 아래 '소비 보류함'에서 언제든 볼 수 있어요.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Button(
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("확인")
    }
}
