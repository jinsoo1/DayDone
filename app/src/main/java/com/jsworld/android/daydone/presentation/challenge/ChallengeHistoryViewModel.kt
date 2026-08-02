package com.jsworld.android.daydone.presentation.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.NoSpendChallengeRecord
import com.jsworld.android.daydone.domain.usecase.ObserveNoSpendChallengeRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class ChallengeHistoryUiState(
    val isLoading: Boolean = true,
    val records: List<NoSpendChallengeRecord> = emptyList()
)

@HiltViewModel
class ChallengeHistoryViewModel @Inject constructor(
    observeNoSpendChallengeRecordsUseCase: ObserveNoSpendChallengeRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeHistoryUiState())
    val uiState: StateFlow<ChallengeHistoryUiState> = _uiState.asStateFlow()

    init {
        observeNoSpendChallengeRecordsUseCase()
            .onEach { records ->
                _uiState.value = ChallengeHistoryUiState(
                    isLoading = false,
                    records = records
                )
            }
            .launchIn(viewModelScope)
    }
}
