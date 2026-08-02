package com.jsworld.android.daydone.presentation.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.Notice
import com.jsworld.android.daydone.domain.usecase.GetNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoticesUiState(
    val isLoading: Boolean = true,
    val notices: List<Notice> = emptyList()
)

@HiltViewModel
class NoticesViewModel @Inject constructor(
    private val getNoticesUseCase: GetNoticesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticesUiState())
    val uiState: StateFlow<NoticesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val notices = runCatching { getNoticesUseCase() }.getOrDefault(emptyList())
            _uiState.value = NoticesUiState(isLoading = false, notices = notices)
        }
    }
}
