package com.jsworld.android.daydone.presentation.monthly.model

import java.time.LocalDate

data class MonthlyDayCellUiModel(
    val date: LocalDate,
    val dayText: String,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasExpense: Boolean,
    val hasScheduledDeduction: Boolean,
    val isNoSpendSuccess: Boolean = false
)
