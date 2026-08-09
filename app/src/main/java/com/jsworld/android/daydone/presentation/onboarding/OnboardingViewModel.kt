package com.jsworld.android.daydone.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.domain.usecase.AddScheduledDeductionUseCase
import com.jsworld.android.daydone.domain.usecase.CompleteOnboardingUseCase
import com.jsworld.android.daydone.domain.usecase.GetCurrentBudgetPeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class OnboardingUiState(
    val monthlyIncomeInput: String = "",
    val budgetStartDayInput: String = "1",

    // 저축/고정비 1건 (선택)
    val deductionTitleInput: String = "",
    val deductionAmountInput: String = "",
    val deductionTypeInput: ScheduledDeductionType = ScheduledDeductionType.SAVING,
    val deductionWithdrawalDayInput: String = "",

    // 미리보기 (파생)
    val previewPureBudget: Long = 0L,
    val previewRemainingDays: Int = 0,
    val previewDailyLine: Long = 0L,
    val isPreviewVisible: Boolean = false
) {
    val monthlyIncome: Long get() = monthlyIncomeInput.toLongOrNull() ?: 0L
    val budgetStartDay: Int get() = budgetStartDayInput.toIntOrNull() ?: 0
    val deductionAmount: Long get() = deductionAmountInput.toLongOrNull() ?: 0L
    val deductionWithdrawalDay: Int get() = deductionWithdrawalDayInput.toIntOrNull() ?: 0

    /** 차감 입력을 시작했다면 완성돼야 저장 가능. 아예 비워두면(선택 안 함) 통과. */
    val isDeductionEmpty: Boolean
        get() = deductionTitleInput.isBlank() &&
                deductionAmountInput.isBlank() &&
                deductionWithdrawalDayInput.isBlank()

    val isDeductionValid: Boolean
        get() = deductionTitleInput.isNotBlank() &&
                deductionAmount > 0L &&
                deductionWithdrawalDay in 1..31

    val canComplete: Boolean
        get() = monthlyIncome > 0L &&
                budgetStartDay in 1..31 &&
                (isDeductionEmpty || isDeductionValid)
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val addScheduledDeductionUseCase: AddScheduledDeductionUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onMonthlyIncomeChange(value: String) {
        update { it.copy(monthlyIncomeInput = value.filter { c -> c.isDigit() }) }
    }

    fun onBudgetStartDayChange(value: String) {
        update { it.copy(budgetStartDayInput = value.filter { c -> c.isDigit() }.take(2)) }
    }

    fun onDeductionTitleChange(value: String) {
        update { it.copy(deductionTitleInput = value) }
    }

    fun onDeductionAmountChange(value: String) {
        update { it.copy(deductionAmountInput = value.filter { c -> c.isDigit() }) }
    }

    fun onDeductionTypeChange(type: ScheduledDeductionType) {
        update { it.copy(deductionTypeInput = type) }
    }

    fun onDeductionWithdrawalDayChange(value: String) {
        update { it.copy(deductionWithdrawalDayInput = value.filter { c -> c.isDigit() }.take(2)) }
    }

    private fun update(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.value = transform(_uiState.value).withPreview()
    }

    /** 입력값으로 "오늘의 방어선"이 어떻게 계산될지 미리 보여준다. */
    private fun OnboardingUiState.withPreview(): OnboardingUiState {
        if (monthlyIncome <= 0L || budgetStartDay !in 1..31) {
            return copy(isPreviewVisible = false)
        }

        val today = LocalDate.now()
        val period = getCurrentBudgetPeriodUseCase(today, budgetStartDay)
        val remainingDays =
            (ChronoUnit.DAYS.between(today, period.endDate).toInt() + 1).coerceAtLeast(1)

        val deduction = if (deductionAmount > 0L) deductionAmount else 0L
        val pure = monthlyIncome - deduction

        return copy(
            isPreviewVisible = true,
            previewPureBudget = pure,
            previewRemainingDays = remainingDays,
            previewDailyLine = if (pure > 0L) pure / remainingDays else 0L
        )
    }

    fun onCompleteClick() {
        val state = _uiState.value
        if (!state.canComplete) return

        viewModelScope.launch {
            completeOnboardingUseCase(
                monthlyIncome = state.monthlyIncome,
                budgetStartDay = state.budgetStartDay
            )

            if (state.isDeductionValid) {
                addScheduledDeductionUseCase(
                    title = state.deductionTitleInput.trim(),
                    amount = state.deductionAmount,
                    type = state.deductionTypeInput,
                    withdrawalDay = state.deductionWithdrawalDay,
                    startYearMonth = YearMonth.now(),
                    endYearMonth = null,
                    memo = null
                )
            }
        }
    }
}
