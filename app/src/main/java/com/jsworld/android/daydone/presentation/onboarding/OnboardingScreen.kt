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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.R
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
                text = stringResource(R.string.onboarding_deideon),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.onboarding_oneul_eolmakkaji_sseodo_doeneunji),
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = stringResource(R.string.onboarding_weol_suibeseo_jeochug_gojeongbi),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = uiState.monthlyIncomeInput,
                onValueChange = onMonthlyIncomeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_weol_suib)) },
                placeholder = { Text(stringResource(R.string.onboarding_ye_3000000)) },
                singleLine = true,
                suffix = { Text(stringResource(R.string.onboarding_weon)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (uiState.monthlyIncome > 0L) {
                        Text(stringResource(R.string.onboarding_maedal_gijuneuro_gyesanhaeyo, uiState.monthlyIncome.toMoneyText()))
                    } else {
                        Text(stringResource(R.string.onboarding_sehu_silsuryeongaeg_gijuneuro_jeogneun))
                    }
                }
            )

            OutlinedTextField(
                value = uiState.budgetStartDayInput,
                onValueChange = onBudgetStartDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_yesan_sijagil)) },
                singleLine = true,
                suffix = { Text(stringResource(R.string.onboarding_il)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(stringResource(R.string.onboarding_han_dal_yesaneul_gyesanhaneun))
                },
                isError = uiState.budgetStartDayInput.isNotBlank() &&
                        uiState.budgetStartDay !in 1..31
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.onboarding_maedal_naganeun_don_hana),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.onboarding_jeoggeumina_weolsecheoreom_maedal_ppajineun),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.deductionTypeInput == ScheduledDeductionType.SAVING,
                    onClick = { onDeductionTypeChange(ScheduledDeductionType.SAVING) },
                    label = { Text(stringResource(R.string.onboarding_jeochug)) }
                )
                FilterChip(
                    selected = uiState.deductionTypeInput == ScheduledDeductionType.FIXED,
                    onClick = { onDeductionTypeChange(ScheduledDeductionType.FIXED) },
                    label = { Text(stringResource(R.string.onboarding_gojeongbi)) }
                )
            }

            OutlinedTextField(
                value = uiState.deductionTitleInput,
                onValueChange = onDeductionTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.onboarding_hangmogmyeong)) },
                placeholder = {
                    Text(
                        if (uiState.deductionTypeInput == ScheduledDeductionType.SAVING) {
                            stringResource(R.string.onboarding_ye_jeoggeum)
                        } else {
                            stringResource(R.string.onboarding_ye_weolse_boheomryo)
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
                    label = { Text(stringResource(R.string.onboarding_geumaeg)) },
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.onboarding_weon)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.deductionWithdrawalDayInput,
                    onValueChange = onDeductionWithdrawalDayChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.onboarding_chulgeumil)) },
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.onboarding_il)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (!uiState.isDeductionEmpty && !uiState.isDeductionValid) {
                Text(
                    text = stringResource(R.string.onboarding_hangmogmyeong_geumaeg_chulgeumil_1),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.isPreviewVisible) {
                PreviewCard(uiState = uiState)
            }

            Text(
                text = stringResource(R.string.onboarding_ibryeoghan_jeongboneun_i_hyudaeponeman),
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
            Text(stringResource(R.string.onboarding_sijaghagi))
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
                text = stringResource(R.string.onboarding_ireohge_gyesandwaeyo),
                style = MaterialTheme.typography.titleMedium
            )

            PreviewRow(stringResource(R.string.onboarding_weol_suib), uiState.monthlyIncome.toMoneyText())

            if (uiState.deductionAmount > 0L) {
                PreviewRow(
                    title = if (uiState.deductionTitleInput.isBlank()) {
                        stringResource(R.string.onboarding_jeochug_gojeongbi)
                    } else {
                        uiState.deductionTitleInput
                    },
                    value = "-${uiState.deductionAmount.toMoneyText()}"
                )
            }

            PreviewRow(stringResource(R.string.onboarding_sseul_su_issneun_saenghwalbi), uiState.previewPureBudget.toMoneyText())

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )

            Text(
                text = stringResource(R.string.onboarding_oneulbuteo_il_dongan, uiState.previewRemainingDays),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Text(
                text = stringResource(R.string.onboarding_haru_aneseo_sseumyeon_gwaenchanhayo, uiState.previewDailyLine.toMoneyText()),
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
