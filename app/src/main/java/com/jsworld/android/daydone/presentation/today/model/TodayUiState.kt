package com.jsworld.android.daydone.presentation.today.model

import com.jsworld.android.daydone.domain.model.NoSpendMode
import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import java.time.LocalDate

data class TodayUiState(
    val todayStartDefenseLine: Long = 0L,
    val todayRemainingDefenseLine: Long = 0L,
    val todayExpenseAmount: Long = 0L,
    val todayOverAmount: Long = 0L,
    val isTodayOverDefenseLine: Boolean = false,
    val remainingPureBudget: Long = 0L,
    val remainingDays: Int = 0,
    /** 내일 권장 금액. 기간 마지막 날이면 null(나눌 내일이 없음) → 줄 자체를 숨긴다 */
    val tomorrowRecommended: Long? = null,
    val budgetPeriodText: String = "",
    val message: String = "",
    val dateChips: List<TodayDateChipUiModel> = emptyList(),
    val selectedDateTitle: String = "오늘 내역",
    val quickExpenses: List<QuickExpenseUiModel> = emptyList(),
    val selectedDateExpenses: List<TodayExpenseUiModel> = emptyList(),
    val selectedDateScheduledDeductions: List<TodayScheduledDeductionUiModel> = emptyList(),

    val selectedDateExtraIncomes: List<TodayExtraIncomeUiModel> = emptyList(),

    val isExpenseInputSheetVisible: Boolean = false,
    val editingExpenseId: Long? = null,
    val expenseTitleInput: String = "",
    val expenseAmountInput: String = "",
    val expenseDateInput: LocalDate = LocalDate.now(),
    val expenseEssentialInput: Boolean = false,
    val showEssentialCheckbox: Boolean = false,

    // 새 기간 첫 며칠간 지난 기간 결산 리포트 안내 ("yyyy-MM", null=숨김)
    val lastPeriodReportMonth: String? = null,

    // 가입 전 지출 안내 (기간 중간 가입 시 1회)
    val showPreJoinBanner: Boolean = false,
    val periodStartLabel: String = "",          // "7월 10일" — 배너/다이얼로그 문구용
    val isPreJoinDialogVisible: Boolean = false,
    val preJoinAmountInput: String = "",

    // 무지출 챌린지
    val challengeEnabled: Boolean = false,
    val challengeMode: NoSpendMode = NoSpendMode.ESSENTIAL_ALLOWED,
    val challengeCapAmount: Long = 0L,
    val challengeTargetDays: Int = 0,
    val challengeSuccessDays: Int = 0,
    val challengeTodayOnTrack: Boolean = false,
    val challengeStreak: Int = 0,
    val challengeDayIndex: Int = 0,
    val challengeFinished: Boolean = false,

    val isBudgetSettingSheetVisible: Boolean = false,
    val monthlyIncomeInput: String = "",
    val budgetStartDayInput: String = "",

    val isScheduledDeductionSheetVisible: Boolean = false,
    val editingScheduledDeductionId: Long? = null,
    val scheduledDeductionTitleInput: String = "",
    val scheduledDeductionAmountInput: String = "",
    val scheduledDeductionWithdrawalDayInput: String = "",
    val scheduledDeductionTypeInput: ScheduledDeductionType = ScheduledDeductionType.SAVING,

    val scheduledSavingAmount: Long = 0L,
    val fixedExpenseAmount: Long = 0L,
    val scheduledDeductionSummaries: List<ScheduledDeductionSummaryUiModel> = emptyList(),

    val isFabMenuExpanded: Boolean = false,

    val isQuickExpenseInputSheetVisible: Boolean = false,
    val quickExpenseTitleInput: String = "",
    val quickExpenseAmountInput: String = "",

    val monthlyIncome: Long = 0L,
    val extraIncomeAmount: Long = 0L,
    val totalAvailableBudget: Long = 0L,
    val scheduledDeductionTotalAmount: Long = 0L,
    val pastExpenseAmount: Long = 0L,

    val isExtraIncomeInputSheetVisible: Boolean = false,
    val editingExtraIncomeId: Long? = null,
    val extraIncomeTitleInput: String = "",
    val extraIncomeAmountInput: String = "",
    val extraIncomeMemoInput: String = "",
    val extraIncomeDateInput: LocalDate = LocalDate.now(),

    // 살까 말까 (+ 시트에서 진입)
    val isPurchaseSheetVisible: Boolean = false,
    val purchaseTitleInput: String = "",
    val purchaseAmountInput: String = "",
    val purchaseResult: PurchaseEvaluationUiModel? = null, // null = 입력 단계
    val purchaseHeldDone: Boolean = false,                 // 보류 저장 후 확인 단계
)