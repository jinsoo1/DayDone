package com.jsworld.android.daydone.presentation.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.NoSpendMode

/**
 * 무지출 챌린지 설정/시작/그만두기 바텀시트.
 * 자체 ViewModel을 가져 오늘 탭·설정 탭 어디서든 그대로 띄울 수 있다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeSettingsSheet(
    onDismiss: () -> Unit,
    viewModel: ChallengeSheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showStopConfirm by remember { mutableStateOf(false) }

    val inputValid = (uiState.targetDaysInput.toIntOrNull() ?: 0) in 1..31 &&
            (uiState.modeInput != NoSpendMode.CAP ||
                    (uiState.capInput.toLongOrNull() ?: 0L) > 0L)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "무지출 챌린지", style = MaterialTheme.typography.titleLarge)

            if (uiState.running) {
                Text(
                    text = "지금 ${uiState.dayIndex}일째 진행 중이에요 (목표 ${uiState.targetDays}일).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "예산 기간과 상관없이 시작한 날부터 이어져요. 그만두면 언제든 새로 시작할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { showStopConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("챌린지 그만두기")
                }
            } else {
                if (uiState.finished) {
                    Text(
                        text = "지난 챌린지가 끝났어요. 새로 시작해볼까요?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(text = "성공 기준", style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.FULL,
                        title = "완전 무지출",
                        desc = "그날 지출이 하나도 없어야 성공",
                        onClick = { viewModel.onModeChange(NoSpendMode.FULL) }
                    )
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.ESSENTIAL_ALLOWED,
                        title = "필수 지출 허용",
                        desc = "'필수 지출'로 표시한 것(교통비 등)은 괜찮아요",
                        onClick = { viewModel.onModeChange(NoSpendMode.ESSENTIAL_ALLOWED) }
                    )
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.CAP,
                        title = "금액 상한",
                        desc = "필수 제외 하루 지출이 정한 금액 이하면 성공",
                        onClick = { viewModel.onModeChange(NoSpendMode.CAP) }
                    )
                }

                if (uiState.modeInput == NoSpendMode.CAP) {
                    OutlinedTextField(
                        value = uiState.capInput,
                        onValueChange = viewModel::onCapChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("하루 허용 금액") },
                        singleLine = true,
                        suffix = { Text("원") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = uiState.targetDaysInput,
                    onValueChange = viewModel::onTargetDaysChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("도전 일수") },
                    singleLine = true,
                    suffix = { Text("일") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("오늘부터 며칠 동안 도전할지 (1~31일)") }
                )

                Text(
                    text = "예산 기간과 무관하게 오늘부터 시작해요. 준비금(금고)·저축·고정비는 무지출을 깨지 않아요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.onStart(onDone = onDismiss) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputValid
                ) {
                    Text("오늘부터 시작하기")
                }
            }
        }
    }

    if (showStopConfirm) {
        AlertDialog(
            onDismissRequest = { showStopConfirm = false },
            title = { Text("챌린지를 그만둘까요?") },
            text = {
                Text("지금까지 ${uiState.dayIndex}일째 진행 중이에요. 그만둬도 언제든 새로 시작할 수 있어요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showStopConfirm = false
                    viewModel.onStop(onDone = onDismiss)
                }) {
                    Text("그만두기", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirm = false }) {
                    Text("계속하기")
                }
            }
        )
    }
}

@Composable
private fun ChallengeModeOption(
    selected: Boolean,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = shape
            )
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
