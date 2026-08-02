package com.jsworld.android.daydone.presentation.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.NoSpendChallengeSettings
import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.usecase.ObserveNoSpendChallengeUseCase
import com.jsworld.android.daydone.domain.usecase.UpdateNoSpendChallengeUseCase
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

data class ChallengeSheetUiState(
    val running: Boolean = false,
    val dayIndex: Int = 0,
    val targetDays: Int = 10,
    val finished: Boolean = false,

    val modeInput: NoSpendMode = NoSpendMode.ESSENTIAL_ALLOWED,
    val capInput: String = "10000",
    val targetDaysInput: String = "10"
)

@HiltViewModel
class ChallengeSheetViewModel @Inject constructor(
    observeNoSpendChallengeUseCase: ObserveNoSpendChallengeUseCase,
    private val updateNoSpendChallengeUseCase: UpdateNoSpendChallengeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeSheetUiState())
    val uiState: StateFlow<ChallengeSheetUiState> = _uiState.asStateFlow()

    private var currentSettings: NoSpendChallengeSettings? = null
    private var inputsInitialized = false

    init {
        observeNoSpendChallengeUseCase()
            .onEach { settings ->
                currentSettings = settings

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
                    running = running,
                    dayIndex = dayIndex,
                    targetDays = settings.targetDays,
                    finished = finished
                )

                // 입력값은 처음 한 번만 저장된 설정으로 채운다 (편집 중 덮어쓰기 방지)
                if (!inputsInitialized) {
                    inputsInitialized = true
                    _uiState.value = _uiState.value.copy(
                        modeInput = settings.mode,
                        capInput = settings.capAmount.toString(),
                        targetDaysInput = settings.targetDays.toString()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onModeChange(mode: NoSpendMode) {
        _uiState.value = _uiState.value.copy(modeInput = mode)
    }

    fun onCapChange(value: String) {
        _uiState.value = _uiState.value.copy(capInput = value.filter { it.isDigit() })
    }

    fun onTargetDaysChange(value: String) {
        _uiState.value = _uiState.value.copy(
            targetDaysInput = value.filter { it.isDigit() }.take(2)
        )
    }

    /** 오늘부터 새 도전 시작. */
    fun onStart(onDone: () -> Unit) {
        val state = _uiState.value
        val cap = state.capInput.toLongOrNull() ?: 0L
        val targetDays = state.targetDaysInput.toIntOrNull() ?: 0

        if (targetDays !in 1..31) return
        if (state.modeInput == NoSpendMode.CAP && cap <= 0L) return

        viewModelScope.launch {
            updateNoSpendChallengeUseCase(
                NoSpendChallengeSettings(
                    enabled = true,
                    mode = state.modeInput,
                    capAmount = if (cap > 0L) cap else 10_000L,
                    targetDays = targetDays,
                    startDate = LocalDate.now()
                )
            )
            onDone()
        }
    }

    /** 챌린지 그만두기 (설정값은 다음을 위해 유지). */
    fun onStop(onDone: () -> Unit) {
        val settings = currentSettings ?: return
        viewModelScope.launch {
            updateNoSpendChallengeUseCase(
                settings.copy(enabled = false, startDate = null)
            )
            onDone()
        }
    }
}
