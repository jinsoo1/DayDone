package com.jsworld.android.daydone.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.usecase.ExportBackupToDownloadsUseCase
import com.jsworld.android.daydone.domain.usecase.ExportBackupUseCase
import com.jsworld.android.daydone.domain.model.BackupFileInfo
import com.jsworld.android.daydone.domain.usecase.ExportExcelUseCase
import com.jsworld.android.daydone.domain.usecase.ImportBackupFromFileUseCase
import com.jsworld.android.daydone.domain.usecase.ImportBackupUseCase
import com.jsworld.android.daydone.domain.usecase.ListBackupFilesUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveBudgetProfileUseCase
import com.jsworld.android.daydone.domain.usecase.ObserveNoSpendChallengeUseCase
import com.jsworld.android.daydone.domain.usecase.ResetAllDataUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateBudgetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SettingsUiState(
    val monthlyIncome: Long = 0L,
    val budgetStartDay: Int = 1,

    // 기본 수입 수정 시트
    val isIncomeSheetVisible: Boolean = false,
    val incomeInput: String = "",

    // 예산 시작일 수정 시트
    val isStartDaySheetVisible: Boolean = false,
    val startDayInput: String = "",

    // 무지출 챌린지 (행 표시용 — 시트는 공용 ChallengeSettingsSheet가 담당)
    val challengeTargetDays: Int = 10,
    val challengeRunning: Boolean = false,   // 지금 진행 중
    val challengeDayIndex: Int = 0,          // 며칠째
    val challengeFinished: Boolean = false,  // 기간 종료됨

    // 백업/복원
    val isBackupWorking: Boolean = false,
    val backupMessage: String? = null,       // 결과 안내 (스낵바 성격)

    // 복원 파일 목록 시트
    val isRestoreSheetVisible: Boolean = false,
    val backupFiles: List<BackupFileInfo> = emptyList(),
    val restoreCandidate: BackupFileInfo? = null   // 확인 다이얼로그에 띄울 파일
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val updateBudgetProfileUseCase: UpdateBudgetProfileUseCase,
    private val observeNoSpendChallengeUseCase: ObserveNoSpendChallengeUseCase,
    private val resetAllDataUseCase: ResetAllDataUseCase,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val exportBackupToDownloadsUseCase: ExportBackupToDownloadsUseCase,
    private val exportExcelUseCase: ExportExcelUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val listBackupFilesUseCase: ListBackupFilesUseCase,
    private val importBackupFromFileUseCase: ImportBackupFromFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeBudgetProfileUseCase()
            .onEach { profile ->
                _uiState.value = _uiState.value.copy(
                    monthlyIncome = profile.monthlyIncome,
                    budgetStartDay = profile.budgetStartDay
                )
            }
            .launchIn(viewModelScope)

        observeNoSpendChallengeUseCase()
            .onEach { settings ->
                val today = LocalDate.now()
                val start = settings.startDate
                val end = start?.plusDays((settings.targetDays - 1).toLong())

                val running = settings.enabled && start != null && end != null &&
                        !today.isBefore(start) && !today.isAfter(end)
                val finished = settings.enabled && end != null && today.isAfter(end)
                val dayIndex = if (running && start != null) {
                    ChronoUnit.DAYS.between(start, today).toInt() + 1
                } else {
                    0
                }

                _uiState.value = _uiState.value.copy(
                    challengeTargetDays = settings.targetDays,
                    challengeRunning = running,
                    challengeDayIndex = dayIndex,
                    challengeFinished = finished
                )
            }
            .launchIn(viewModelScope)
    }

    // --- 기본 수입 ---

    fun onIncomeClick() {
        _uiState.value = _uiState.value.copy(
            isIncomeSheetVisible = true,
            incomeInput = _uiState.value.monthlyIncome.toString()
        )
    }

    fun onIncomeChange(value: String) {
        _uiState.value = _uiState.value.copy(incomeInput = value.filter { it.isDigit() })
    }

    fun onIncomeDismiss() {
        _uiState.value = _uiState.value.copy(isIncomeSheetVisible = false)
    }

    fun onIncomeSave() {
        val income = _uiState.value.incomeInput.toLongOrNull() ?: 0L
        if (income <= 0L) return
        viewModelScope.launch {
            updateBudgetProfileUseCase.updateMonthlyIncome(income)
            _uiState.value = _uiState.value.copy(isIncomeSheetVisible = false)
        }
    }

    // --- 예산 시작일 ---

    fun onStartDayClick() {
        _uiState.value = _uiState.value.copy(
            isStartDaySheetVisible = true,
            startDayInput = _uiState.value.budgetStartDay.toString()
        )
    }

    fun onStartDayChange(value: String) {
        _uiState.value = _uiState.value.copy(
            startDayInput = value.filter { it.isDigit() }.take(2)
        )
    }

    fun onStartDayDismiss() {
        _uiState.value = _uiState.value.copy(isStartDaySheetVisible = false)
    }

    fun onStartDaySave() {
        val day = _uiState.value.startDayInput.toIntOrNull() ?: 0
        if (day !in 1..31) return
        viewModelScope.launch {
            updateBudgetProfileUseCase.updateBudgetStartDay(day)
            _uiState.value = _uiState.value.copy(isStartDaySheetVisible = false)
        }
    }

    // --- 백업 / 복원 ---

    /**
     * 기본은 다운로드/DayDone 폴더에 바로 저장.
     * 그게 안 되는 환경(Android 9 등)이면 [onNeedPicker] 로 JSON 을 넘겨 파일 선택창을 띄운다.
     */
    fun onExportRequested(onNeedPicker: (String) -> Unit) {
        if (_uiState.value.isBackupWorking) return
        _uiState.value = _uiState.value.copy(isBackupWorking = true, backupMessage = null)

        viewModelScope.launch {
            runCatching { exportBackupToDownloadsUseCase() }
                .onSuccess { path ->
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = "백업을 저장했어요.\n$path"
                    )
                }
                .onFailure {
                    // 공용 폴더에 못 쓰는 경우 → 저장 위치를 직접 고르게 한다
                    runCatching { exportBackupUseCase() }
                        .onSuccess { json ->
                            _uiState.value = _uiState.value.copy(isBackupWorking = false)
                            onNeedPicker(json)
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                isBackupWorking = false,
                                backupMessage = "내보내기에 실패했어요. 다시 시도해 주세요."
                            )
                        }
                }
        }
    }

    /** 지출 내역(월별)·고정 지출 시트가 담긴 엑셀 파일을 다운로드/DayDone 폴더에 저장. */
    fun onExportExcelClick() {
        if (_uiState.value.isBackupWorking) return
        _uiState.value = _uiState.value.copy(isBackupWorking = true, backupMessage = null)

        viewModelScope.launch {
            runCatching { exportExcelUseCase() }
                .onSuccess { path ->
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = "엑셀 파일을 저장했어요. 월별 지출 내역과 고정 지출 시트가 들어있어요.\n$path"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = "내보내기에 실패했어요. 다시 시도해 주세요."
                    )
                }
        }
    }

    fun onExportSaved() {
        _uiState.value = _uiState.value.copy(backupMessage = "백업 파일을 저장했어요.")
    }

    // --- 복원 파일 목록 시트 ---

    fun onRestoreClick() {
        if (_uiState.value.isBackupWorking) return
        viewModelScope.launch {
            val files = runCatching { listBackupFilesUseCase() }.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isRestoreSheetVisible = true,
                backupFiles = files
            )
        }
    }

    fun onRestoreSheetDismiss() {
        _uiState.value = _uiState.value.copy(
            isRestoreSheetVisible = false,
            restoreCandidate = null
        )
    }

    fun onRestoreFileClick(file: BackupFileInfo) {
        _uiState.value = _uiState.value.copy(restoreCandidate = file)
    }

    fun onRestoreCandidateDismiss() {
        _uiState.value = _uiState.value.copy(restoreCandidate = null)
    }

    fun onRestoreCandidateConfirm() {
        val file = _uiState.value.restoreCandidate ?: return
        if (_uiState.value.isBackupWorking) return
        _uiState.value = _uiState.value.copy(
            isBackupWorking = true,
            isRestoreSheetVisible = false,
            restoreCandidate = null,
            backupMessage = null
        )

        viewModelScope.launch {
            runCatching { importBackupFromFileUseCase(file.uri) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = "복원했어요. 오늘 탭에서 확인해보세요."
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = e.message?.takeIf { it.isNotBlank() }
                            ?: "파일을 읽을 수 없어요. 폴더에서 직접 선택해 주세요."
                    )
                }
        }
    }

    fun onImportSelected(json: String) {
        if (_uiState.value.isBackupWorking) return
        _uiState.value = _uiState.value.copy(isBackupWorking = true, backupMessage = null)

        viewModelScope.launch {
            runCatching { importBackupUseCase(json) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = "복원했어요. 오늘 탭에서 확인해보세요."
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isBackupWorking = false,
                        backupMessage = e.message?.takeIf { it.isNotBlank() }
                            ?: "파일을 읽을 수 없어요. 데이던 백업 파일인지 확인해 주세요."
                    )
                }
        }
    }

    fun onBackupMessageShown() {
        _uiState.value = _uiState.value.copy(backupMessage = null)
    }

    // --- 데이터 초기화 ---

    fun onResetConfirm() {
        viewModelScope.launch {
            resetAllDataUseCase()
        }
    }
}
