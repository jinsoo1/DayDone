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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.R
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
            Text(text = stringResource(R.string.challenge_settings_mujichul_chaelrinji), style = MaterialTheme.typography.titleLarge)

            if (uiState.running) {
                Text(
                    text = stringResource(R.string.challenge_settings_jigeum_iljjae_jinhaeng_jungieyo, uiState.dayIndex, uiState.targetDays),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.challenge_settings_yesan_gigangwa_sanggwaneobsi_sijaghan),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { showStopConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.challenge_settings_chaelrinji_geumandugi))
                }
            } else {
                if (uiState.finished) {
                    Text(
                        text = stringResource(R.string.challenge_settings_jinan_chaelrinjiga_kkeutnasseoyo_saero),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(text = stringResource(R.string.challenge_settings_seonggong_gijun), style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.FULL,
                        title = stringResource(R.string.challenge_history_wanjeon_mujichul),
                        desc = stringResource(R.string.challenge_settings_geunal_jichuli_hanado_eobseoya),
                        onClick = { viewModel.onModeChange(NoSpendMode.FULL) }
                    )
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.ESSENTIAL_ALLOWED,
                        title = stringResource(R.string.challenge_history_pilsu_jichul_heoyong),
                        desc = stringResource(R.string.challenge_settings_pilsu_jichul_ro_pyosihan),
                        onClick = { viewModel.onModeChange(NoSpendMode.ESSENTIAL_ALLOWED) }
                    )
                    ChallengeModeOption(
                        selected = uiState.modeInput == NoSpendMode.CAP,
                        title = stringResource(R.string.challenge_settings_geumaeg_sanghan),
                        desc = stringResource(R.string.challenge_settings_pilsu_jeoe_haru_jichuli),
                        onClick = { viewModel.onModeChange(NoSpendMode.CAP) }
                    )
                }

                if (uiState.modeInput == NoSpendMode.CAP) {
                    OutlinedTextField(
                        value = uiState.capInput,
                        onValueChange = viewModel::onCapChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.challenge_settings_haru_heoyong_geumaeg)) },
                        singleLine = true,
                        suffix = { Text(stringResource(R.string.challenge_settings_weon)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = uiState.targetDaysInput,
                    onValueChange = viewModel::onTargetDaysChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.challenge_settings_dojeon_ilsu)) },
                    singleLine = true,
                    suffix = { Text(stringResource(R.string.challenge_settings_il)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text(stringResource(R.string.challenge_settings_oneulbuteo_myeochil_dongan_dojeonhalji)) }
                )

                Text(
                    text = stringResource(R.string.challenge_settings_yesan_gigangwa_mugwanhage_oneulbuteo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.onStart(onDone = onDismiss) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputValid
                ) {
                    Text(stringResource(R.string.challenge_settings_oneulbuteo_sijaghagi))
                }
            }
        }
    }

    if (showStopConfirm) {
        AlertDialog(
            onDismissRequest = { showStopConfirm = false },
            title = { Text(stringResource(R.string.challenge_settings_chaelrinjireul_geumandulkkayo)) },
            text = {
                Text(stringResource(R.string.challenge_settings_jigeumkkaji_iljjae_jinhaeng_jungieyo, uiState.dayIndex))
            },
            confirmButton = {
                TextButton(onClick = {
                    showStopConfirm = false
                    viewModel.onStop(onDone = onDismiss)
                }) {
                    Text(stringResource(R.string.challenge_settings_geumandugi), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirm = false }) {
                    Text(stringResource(R.string.challenge_settings_gyesoghagi))
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
