package com.jsworld.android.daydone.presentation.today.model

import java.time.LocalDate

data class TodayDateChipUiModel(
    val date: LocalDate,
    val dayText: String,
    val weekText: String,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasExpense: Boolean,
    val hasScheduledDeduction: Boolean
)