package com.jsworld.android.daydone.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.BackupFileInfo
import com.jsworld.android.daydone.presentation.challenge.ChallengeSettingsSheet
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.component.NoticeBox

@Composable
fun SettingsRoute(
    onNavigateToNotices: () -> Unit = {},
    onNavigateToChallengeHistory: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 내보내기: 저장 위치를 고르면 그때 JSON 을 써 넣는다
    var pendingJson by remember { mutableStateOf<String?>(null) }
    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingJson
        pendingJson = null
        if (uri != null && json != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray())
                }
            }.onSuccess { viewModel.onExportSaved() }
        }
    }

    // 가져오기: 파일을 읽어 ViewModel 로 넘긴다 (선택창은 다운로드/DayDone/백업 폴더에서 시작)
    val openFileLauncher = rememberLauncherForActivityResult(
        OpenBackupDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()?.let { viewModel.onImportSelected(it) }
        }
    }

    SettingsScreen(
        onExportClick = {
            viewModel.onExportRequested { json ->
                pendingJson = json
                createFileLauncher.launch(defaultBackupFileName())
            }
        },
        onImportClick = {
            openFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        },
        onExportExcelClick = viewModel::onExportExcelClick,
        onRestoreClick = viewModel::onRestoreClick,
        onRestoreSheetDismiss = viewModel::onRestoreSheetDismiss,
        onRestoreFileClick = viewModel::onRestoreFileClick,
        onRestoreCandidateDismiss = viewModel::onRestoreCandidateDismiss,
        onRestoreCandidateConfirm = viewModel::onRestoreCandidateConfirm,
        onBackupMessageShown = viewModel::onBackupMessageShown,
        uiState = uiState,
        onNavigateToNotices = onNavigateToNotices,
        onNavigateToChallengeHistory = onNavigateToChallengeHistory,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onIncomeClick = viewModel::onIncomeClick,
        onIncomeChange = viewModel::onIncomeChange,
        onIncomeDismiss = viewModel::onIncomeDismiss,
        onIncomeSave = viewModel::onIncomeSave,
        onStartDayClick = viewModel::onStartDayClick,
        onStartDayChange = viewModel::onStartDayChange,
        onStartDayDismiss = viewModel::onStartDayDismiss,
        onStartDaySave = viewModel::onStartDaySave,
        onResetConfirm = viewModel::onResetConfirm
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onExportExcelClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onRestoreSheetDismiss: () -> Unit = {},
    onRestoreFileClick: (BackupFileInfo) -> Unit = {},
    onRestoreCandidateDismiss: () -> Unit = {},
    onRestoreCandidateConfirm: () -> Unit = {},
    onBackupMessageShown: () -> Unit = {},
    onNavigateToNotices: () -> Unit,
    onNavigateToChallengeHistory: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit = {},
    onIncomeClick: () -> Unit,
    onIncomeChange: (String) -> Unit,
    onIncomeDismiss: () -> Unit,
    onIncomeSave: () -> Unit,
    onStartDayClick: () -> Unit,
    onStartDayChange: (String) -> Unit,
    onStartDayDismiss: () -> Unit,
    onStartDaySave: () -> Unit,
    onResetConfirm: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    var showChallengeSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        DayDoneTopBar(title = stringResource(R.string.settings_seoljeong))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_gongjisahang),
                            value = "",
                            onClick = onNavigateToNotices
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_weol_suib),
                            value = uiState.monthlyIncome.toMoneyText(),
                            onClick = onIncomeClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = stringResource(R.string.settings_yesan_sijagil),
                            value = if (uiState.budgetStartDay == 31) {
                                stringResource(R.string.settings_maeweol_malil)
                            } else {
                                stringResource(R.string.settings_maeweol_il, uiState.budgetStartDay)
                            },
                            onClick = onStartDayClick
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_mujichul_chaelrinji),
                            value = when {
                                uiState.challengeRunning ->
                                    stringResource(R.string.settings_iljjae_jinhaeng_jung, uiState.challengeDayIndex, uiState.challengeTargetDays)
                                uiState.challengeFinished ->
                                    stringResource(R.string.settings_jinan_chaelrinji_wanryo)
                                else ->
                                    stringResource(R.string.settings_sijaghagi)
                            },
                            onClick = { showChallengeSheet = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = stringResource(R.string.settings_dojeon_girog),
                            value = "",
                            onClick = onNavigateToChallengeHistory
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = stringResource(R.string.settings_alrim),
                            value = if (uiState.notificationOnCount > 0) {
                                stringResource(R.string.settings_gae_kyeojim, uiState.notificationOnCount)
                            } else {
                                stringResource(R.string.settings_kkeojim)
                            },
                            onClick = onNavigateToNotificationSettings
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_jeochug_gojeongbineun_oneul_taeb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_deiteo_baegeob_daunrodeu_poldeoe),
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onExportClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = stringResource(R.string.settings_baegeob_paileseo_bogweon),
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onRestoreClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = stringResource(R.string.settings_jichul_naeyeog_naebonaegi_egsel),
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onExportExcelClick
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_paileun_nae_pail_daunrodeu),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = stringResource(R.string.settings_modeun_girogeun_i_hyudaepon),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = stringResource(R.string.settings_deiteo_chogihwa),
                            value = "",
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { showResetConfirm = true }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isIncomeSheetVisible) {
        SettingInputSheet(
            title = stringResource(R.string.settings_weol_suib_sujeong),
            description = stringResource(R.string.settings_ibeon_giganbuteo_jeogyongdwaeyo_jinan),
            label = stringResource(R.string.settings_weol_suib),
            suffix = stringResource(R.string.settings_weon),
            value = uiState.incomeInput,
            onValueChange = onIncomeChange,
            saveEnabled = (uiState.incomeInput.toLongOrNull() ?: 0L) > 0L,
            onSave = onIncomeSave,
            onDismiss = onIncomeDismiss
        )
    }

    if (uiState.isStartDaySheetVisible) {
        SettingInputSheet(
            title = stringResource(R.string.settings_yesan_sijagil_sujeong),
            description = stringResource(R.string.settings_han_dal_yesaneul_gyesanhaneun),
            label = stringResource(R.string.settings_yesan_sijagil),
            suffix = stringResource(R.string.settings_il),
            value = uiState.startDayInput,
            onValueChange = onStartDayChange,
            saveEnabled = (uiState.startDayInput.toIntOrNull() ?: 0) in 1..31,
            onSave = onStartDaySave,
            onDismiss = onStartDayDismiss,
            notice = {
                NoticeBox(
                    title = stringResource(R.string.settings_bakkumyeon_modeun_gigani_dasi),
                    lines = listOf(
                        stringResource(R.string.settings_jinan_girogeul_pohamhan_modeun),
                        stringResource(R.string.settings_naljjae_ttara_jichul_jeochug),
                        stringResource(R.string.settings_ye_10il_25ilro_bakkumyeon)
                    )
                )
            }
        )
    }

    if (showChallengeSheet) {
        ChallengeSettingsSheet(onDismiss = { showChallengeSheet = false })
    }

    if (uiState.isRestoreSheetVisible) {
        RestoreSheet(
            files = uiState.backupFiles,
            onFileClick = onRestoreFileClick,
            onPickFromFolder = {
                onRestoreSheetDismiss()
                onImportClick()
            },
            onDismiss = onRestoreSheetDismiss
        )
    }

    uiState.restoreCandidate?.let { file ->
        AlertDialog(
            onDismissRequest = onRestoreCandidateDismiss,
            title = { Text(stringResource(R.string.settings_i_baegeobeuro_bogweonhalkkayo)) },
            text = {
                Text(
                    stringResource(R.string.settings_jigeum_aebe_issneun_jichul, file.name, file.subtitle())
                )
            },
            confirmButton = {
                TextButton(onClick = onRestoreCandidateConfirm) { Text(stringResource(R.string.settings_bogweon)) }
            },
            dismissButton = {
                TextButton(onClick = onRestoreCandidateDismiss) { Text(stringResource(R.string.settings_chwiso)) }
            }
        )
    }

    uiState.backupMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onBackupMessageShown,
            title = { Text(stringResource(R.string.settings_baegeob)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onBackupMessageShown) { Text(stringResource(R.string.settings_hwagin)) }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.settings_modeun_deiteoreul_jiulkkayo)) },
            text = {
                Text(stringResource(R.string.settings_jichul_suig_jeochug_gojeongbi))
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onResetConfirm()
                }) {
                    Text(stringResource(R.string.settings_chogihwa), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.settings_chwiso))
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingInputSheet(
    title: String,
    description: String,
    label: String,
    suffix: String,
    value: String,
    onValueChange: (String) -> Unit,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    notice: (@Composable () -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            notice?.invoke()

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                singleLine = true,
                suffix = { Text(suffix) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = saveEnabled
            ) {
                Text(stringResource(R.string.settings_jeojang))
            }
        }
    }
}

/** 복원할 백업 파일 목록 시트. 재설치 후에는 목록이 비므로 폴더 직접 선택으로 안내한다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreSheet(
    files: List<BackupFileInfo>,
    onFileClick: (BackupFileInfo) -> Unit,
    onPickFromFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.settings_baegeob_paileseo_bogweon), style = MaterialTheme.typography.titleLarge)

            NoticeBox(
                title = stringResource(R.string.settings_bogweonhamyeon_jigeum_deiteoga_modu),
                lines = listOf(
                    stringResource(R.string.settings_jichul_jeochug_gojeongbi_geumgo),
                    stringResource(R.string.settings_doedolril_su_eobseuni_jigeum)
                )
            )

            if (files.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_pyosihal_baegeob_paili_eobseoyo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(files, key = { it.uri }) { file ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFileClick(file) }
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = file.subtitle(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Text(
                    text = stringResource(R.string.settings_i_aebeseo_mandeun_baegeobman),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onPickFromFolder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_poldeoeseo_jigjeob_seontaeg))
            }
        }
    }
}

/** "2026년 7월 26일 · 12KB" 형태의 보조 설명. */
@Composable
private fun BackupFileInfo.subtitle(): String {
    val date = java.time.Instant.ofEpochMilli(modifiedAtMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val kb = ((sizeBytes + 1023) / 1024).coerceAtLeast(1)
    return stringResource(
        R.string.settings_backup_file_subtitle,
        date.year, date.monthValue, date.dayOfMonth, kb
    )
}

/** daydone-backup-20260726.json 형태의 기본 파일명. */
private fun defaultBackupFileName(): String {
    val today = java.time.LocalDate.now()
    return "daydone-backup-%04d%02d%02d.json".format(today.year, today.monthValue, today.dayOfMonth)
}

/**
 * 복원용 파일 선택창을 백업 저장 폴더(다운로드/DayDone/백업)에서 열리게 하는 계약.
 * 폴더 경로는 BackupRepositoryImpl 의 저장 위치와 맞춰야 한다.
 * 일부 기기는 초기 폴더 지정을 무시할 수 있는데, 그때는 기본 위치에서 열릴 뿐이다.
 */
private class OpenBackupDocument : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: android.content.Context, input: Array<String>) =
        super.createIntent(context, input).apply {
            putExtra(
                android.provider.DocumentsContract.EXTRA_INITIAL_URI,
                android.provider.DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    // 루트 "DayDone" 은 고정, 하위 폴더는 저장 쪽(BackupRepositoryImpl)과 같은 리소스
                    "primary:Download/DayDone/" + context.getString(R.string.folder_backup)
                )
            )
        }
}
