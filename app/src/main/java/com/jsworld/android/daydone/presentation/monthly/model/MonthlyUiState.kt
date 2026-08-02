package com.jsworld.android.daydone.presentation.monthly.model

import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import com.jsworld.android.daydone.presentation.today.model.ScheduledDeductionSummaryUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExpenseUiModel
import com.jsworld.android.daydone.presentation.today.model.TodayExtraIncomeUiModel
import java.time.LocalDate

enum class MonthViewMode {
    PAST,    // 지나간 달 → 결산
    CURRENT, // 이번 달 → 진행 중
    FUTURE   // 다가올 달 → 계획
}

data class MonthlyUiState(
    val monthTitle: String = "",
    val periodText: String = "",
    val anchorMonthValue: String = "", // "yyyy-MM", 결산 리포트 진입용
    val canGoPrevious: Boolean = true, // 앱 시작 달 이전으로는 이동 불가
    val mode: MonthViewMode = MonthViewMode.CURRENT,

    val monthlyBudget: Long = 0L,
    val extraIncomeAmount: Long = 0L,
    val totalAvailableBudget: Long = 0L,

    val scheduledSavingAmount: Long = 0L,
    val fixedExpenseAmount: Long = 0L,
    val scheduledDeductionTotalAmount: Long = 0L,

    val totalExpense: Long = 0L,
    val remainingAmount: Long = 0L,

    val scheduledDeductionSummaries: List<ScheduledDeductionSummaryUiModel> = emptyList(),

    val calendarWeeks: List<List<MonthlyDayCellUiModel?>> = emptyList(),

    val selectedDateTitle: String = "",
    val selectedDateExpenses: List<TodayExpenseUiModel> = emptyList(),
    val selectedDateExtraIncomes: List<TodayExtraIncomeUiModel> = emptyList(),
    val selectedDateScheduledDeductions: List<ScheduledDeductionSummaryUiModel> = emptyList(),

    val isBudgetSheetVisible: Boolean = false,
    val budgetInput: String = "",

    // 지출 수정
    val isExpenseSheetVisible: Boolean = false,
    val editingExpenseId: Long? = null,
    val expenseTitleInput: String = "",
    val expenseAmountInput: String = "",
    val expenseDateInput: LocalDate = LocalDate.now(),
    val expenseEssentialInput: Boolean = false,
    val showEssentialCheckbox: Boolean = false,

    // 추가 수익 수정
    val isExtraIncomeSheetVisible: Boolean = false,
    val editingExtraIncomeId: Long? = null,
    val extraIncomeTitleInput: String = "",
    val extraIncomeAmountInput: String = "",
    val extraIncomeMemoInput: String = "",
    val extraIncomeDateInput: LocalDate = LocalDate.now(),

    // 저축 / 고정비 수정
    val isScheduledDeductionSheetVisible: Boolean = false,
    val editingScheduledDeductionId: Long? = null,
    val scheduledDeductionTitleInput: String = "",
    val scheduledDeductionAmountInput: String = "",
    val scheduledDeductionWithdrawalDayInput: String = "",
    val scheduledDeductionTypeInput: ScheduledDeductionType = ScheduledDeductionType.SAVING
)
