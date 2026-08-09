package com.jsworld.android.daydone.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.presentation.util.toMoneyText

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onMonthlyIncomeChange = viewModel::onMonthlyIncomeChange,
        onBudgetStartDayChange = viewModel::onBudgetStartDayChange,
        onDeductionTitleChange = viewModel::onDeductionTitleChange,
        onDeductionAmountChange = viewModel::onDeductionAmountChange,
        onDeductionTypeChange = viewModel::onDeductionTypeChange,
        onDeductionWithdrawalDayChange = viewModel::onDeductionWithdrawalDayChange,
        onCompleteClick = viewModel::onCompleteClick
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onMonthlyIncomeChange: (String) -> Unit,
    onBudgetStartDayChange: (String) -> Unit,
    onDeductionTitleChange: (String) -> Unit,
    onDeductionAmountChange: (String) -> Unit,
    onDeductionTypeChange: (ScheduledDeductionType) -> Unit,
    onDeductionWithdrawalDayChange: (String) -> Unit,
    onCompleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "데이던",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "오늘 얼마까지 써도\n되는지 알려드릴게요",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "월 수입에서 저축·고정비·미리 준비할 돈을 먼저 빼두고, 남은 돈만 마음 편히 쓰도록 도와드려요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.monthlyIncomeInput,
                onValueChange = onMonthlyIncomeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("월 수입") },
                placeholder = { Text("예: 3000000") },
                singleLine = true,
                suffix = { Text("원") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (uiState.monthlyIncome > 0L) {
                        Text("매달 ${uiState.monthlyIncome.toMoneyText()} 기준으로 계산해요.")
                    } else {
                        Text("세후 실수령액 기준으로 적는 걸 추천해요.")
                    }
                }
            )

            OutlinedTextField(
                value = uiState.budgetStartDayInput,
                onValueChange = onBudgetStartDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("예산 시작일") },
                singleLine = true,
                suffix = { Text("일") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text("한 달 예산을 계산하는 기준일이에요 (1~31일). 월급일이 말일이면 31일로 적어주세요 — 달에 없는 날짜는 말일로 계산돼요.")
                },
                isError = uiState.budgetStartDayInput.isNotBlank() &&
                        uiState.budgetStartDay !in 1..31
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "매달 나가는 돈 하나 빼둘까요? (선택)",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "적금이나 월세처럼 매달 빠지는 돈을 하나 등록하면, 그만큼 미리 제외된 예산을 바로 볼 수 있어요. 나머지는 나중에 추가하면 돼요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.deductionTypeInput == ScheduledDeductionType.SAVING,
                    onClick = { onDeductionTypeChange(ScheduledDeductionType.SAVING) },
                    label = { Text("저축") }
                )
                FilterChip(
                    selected = uiState.deductionTypeInput == ScheduledDeductionType.FIXED,
                    onClick = { onDeductionTypeChange(ScheduledDeductionType.FIXED) },
                    label = { Text("고정비") }
                )
            }

            OutlinedTextField(
                value = uiState.deductionTitleInput,
                onValueChange = onDeductionTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("항목명") },
                placeholder = {
                    Text(
                        if (uiState.deductionTypeInput == ScheduledDeductionType.SAVING) {
                            "예: 적금"
                        } else {
                            "예: 월세, 보험료"
                        }
                    )
                },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.deductionAmountInput,
                    onValueChange = onDeductionAmountChange,
                    modifier = Modifier.weight(1.6f),
                    label = { Text("금액") },
                    singleLine = true,
                    suffix = { Text("원") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.deductionWithdrawalDayInput,
                    onValueChange = onDeductionWithdrawalDayChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("출금일") },
                    singleLine = true,
                    suffix = { Text("일") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (!uiState.isDeductionEmpty && !uiState.isDeductionValid) {
                Text(
                    text = "항목명·금액·출금일(1~31)을 모두 채우거나, 모두 비워두세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.isPreviewVisible) {
                PreviewCard(uiState = uiState)
            }

            Text(
                text = "🔒 입력한 정보는 이 휴대폰에만 저장돼요. 서버로 보내지 않아서 유출될 곳이 없어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onCompleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            enabled = uiState.canComplete
        ) {
            Text("시작하기")
        }
    }
}

@Composable
private fun PreviewCard(uiState: OnboardingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "이렇게 계산돼요",
                style = MaterialTheme.typography.titleMedium
            )

            PreviewRow("월 수입", uiState.monthlyIncome.toMoneyText())

            if (uiState.deductionAmount > 0L) {
                PreviewRow(
                    title = if (uiState.deductionTitleInput.isBlank()) {
                        "저축/고정비"
                    } else {
                        uiState.deductionTitleInput
                    },
                    value = "-${uiState.deductionAmount.toMoneyText()}"
                )
            }

            PreviewRow("쓸 수 있는 생활비", uiState.previewPureBudget.toMoneyText())

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )

            Text(
                text = "오늘부터 ${uiState.previewRemainingDays}일 동안",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Text(
                text = "하루 ${uiState.previewDailyLine.toMoneyText()} 안에서 쓰면 괜찮아요",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PreviewRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
