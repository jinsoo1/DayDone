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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.model.BackupFileInfo
import com.jsworld.android.daydone.presentation.challenge.ChallengeSettingsSheet
import com.jsworld.android.daydone.presentation.util.toMoneyText
import com.jsworld.android.daydone.ui.component.DayDoneTopBar
import com.jsworld.android.daydone.ui.component.NoticeBox

@Composable
fun SettingsRoute(
    onNavigateToNotices: () -> Unit = {},
    onNavigateToChallengeHistory: () -> Unit = {},
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
        DayDoneTopBar(title = "설정")

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
                            title = "공지사항",
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
                            title = "월 수입 (기본값)",
                            value = uiState.monthlyIncome.toMoneyText(),
                            onClick = onIncomeClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = "예산 시작일",
                            value = "매월 ${uiState.budgetStartDay}일",
                            onClick = onStartDayClick
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = "무지출 챌린지",
                            value = when {
                                uiState.challengeRunning ->
                                    "${uiState.challengeDayIndex}/${uiState.challengeTargetDays}일째 진행 중"
                                uiState.challengeFinished ->
                                    "지난 챌린지 완료"
                                else ->
                                    "시작하기"
                            },
                            onClick = { showChallengeSheet = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = "도전 기록",
                            value = "",
                            onClick = onNavigateToChallengeHistory
                        )
                    }
                }
            }

            item {
                Text(
                    text = "저축·고정비는 오늘 탭 하단 + 버튼으로, 준비 항목은 금고 탭에서 관리할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = "데이터 백업 (다운로드 폴더에 저장)",
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onExportClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = "백업 파일에서 복원",
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onRestoreClick
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                        SettingsRow(
                            title = "지출 내역 내보내기 (엑셀)",
                            value = "",
                            enabled = !uiState.isBackupWorking,
                            onClick = onExportExcelClick
                        )
                    }
                }
            }

            item {
                Text(
                    text = "파일은 내 파일 → 다운로드 → DayDone 폴더에 저장돼요 " +
                            "(백업은 백업 폴더, 엑셀 파일은 엑셀 폴더). " +
                            "기기를 바꾸거나 앱을 다시 설치하면 데이터가 사라지니, " +
                            "백업을 가끔 만들어 두면 그대로 되살릴 수 있어요. " +
                            "엑셀 파일에는 월별 지출 내역과 고정 지출이 시트로 나뉘어 담겨요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "🔒 모든 기록은 이 휴대폰 안에만 저장돼요. " +
                            "데이던은 서버가 없어서, 내 지출 내역이 밖으로 나가거나 유출될 곳 자체가 없어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRow(
                            title = "데이터 초기화",
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
            title = "월 수입 수정",
            description = "달마다 예산을 따로 정하지 않았을 때 쓰는 기본값이에요. 특정 달의 예산은 월 탭에서 바꿀 수 있어요.",
            label = "월 수입",
            suffix = "원",
            value = uiState.incomeInput,
            onValueChange = onIncomeChange,
            saveEnabled = (uiState.incomeInput.toLongOrNull() ?: 0L) > 0L,
            onSave = onIncomeSave,
            onDismiss = onIncomeDismiss
        )
    }

    if (uiState.isStartDaySheetVisible) {
        SettingInputSheet(
            title = "예산 시작일 수정",
            description = "한 달 예산을 계산하는 기준일이에요 (1~28일).",
            label = "예산 시작일",
            suffix = "일",
            value = uiState.startDayInput,
            onValueChange = onStartDayChange,
            saveEnabled = (uiState.startDayInput.toIntOrNull() ?: 0) in 1..28,
            onSave = onStartDaySave,
            onDismiss = onStartDayDismiss,
            notice = {
                NoticeBox(
                    title = "바꾸면 모든 기간이 다시 나뉘어요",
                    lines = listOf(
                        "지난 기록을 포함한 모든 달이 새 시작일 기준으로 다시 계산돼요.",
                        "날짜에 따라 지출·저축/고정비가 옆 달 기간으로 이동해 보일 수 있어요.",
                        "예: 10일 → 25일로 바꾸면, 7월 15일 지출은 \"6월\" 기간(6/25~7/24)에 속하게 돼요."
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
            title = { Text("이 백업으로 복원할까요?") },
            text = {
                Text(
                    "${file.name}\n${file.subtitle()}\n\n" +
                            "⚠️ 지금 앱에 있는 지출·저축/고정비·금고·예산이 모두 이 파일의 내용으로 바뀌고, 되돌릴 수 없어요."
                )
            },
            confirmButton = {
                TextButton(onClick = onRestoreCandidateConfirm) { Text("복원") }
            },
            dismissButton = {
                TextButton(onClick = onRestoreCandidateDismiss) { Text("취소") }
            }
        )
    }

    uiState.backupMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onBackupMessageShown,
            title = { Text("백업") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onBackupMessageShown) { Text("확인") }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("모든 데이터를 지울까요?") },
            text = {
                Text("지출·수익·저축/고정비·준비 항목·예산 설정이 모두 삭제되고 처음 상태로 돌아가요. 되돌릴 수 없어요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onResetConfirm()
                }) {
                    Text("초기화", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("취소")
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
                Text("저장")
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
            Text(text = "백업 파일에서 복원", style = MaterialTheme.typography.titleLarge)

            NoticeBox(
                title = "⚠️ 복원하면 지금 데이터가 모두 바뀌어요",
                lines = listOf(
                    "지출·저축/고정비·금고·예산이 선택한 백업 파일의 내용으로 전부 교체돼요.",
                    "되돌릴 수 없으니, 지금 데이터가 필요하면 먼저 백업을 만들어 두세요."
                )
            )

            if (files.isEmpty()) {
                Text(
                    text = "표시할 백업 파일이 없어요.\n\n" +
                            "앱을 다시 설치했거나 기기를 바꿨다면 여기 목록에는 안 보일 수 있어요. " +
                            "그래도 파일은 내 파일 → 다운로드 → DayDone → 백업 폴더에 그대로 있으니, " +
                            "아래 버튼으로 직접 골라주세요.",
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
                    text = "이 앱에서 만든 백업만 보여요. 다른 기기나 재설치 전 파일은 아래 버튼으로 직접 골라주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onPickFromFolder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("폴더에서 직접 선택")
            }
        }
    }
}

/** "2026년 7월 26일 · 12KB" 형태의 보조 설명. */
private fun BackupFileInfo.subtitle(): String {
    val date = java.time.Instant.ofEpochMilli(modifiedAtMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    val kb = ((sizeBytes + 1023) / 1024).coerceAtLeast(1)
    return "%d년 %d월 %d일 · %dKB".format(date.year, date.monthValue, date.dayOfMonth, kb)
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
                    "primary:Download/DayDone/백업"
                )
            )
        }
}
