package com.jsworld.android.daydone.presentation.today.model

import java.time.LocalDate

data class TodayExpenseUiModel(
    val id: Long,
    val title: String,
    val amount: Long,
    val date: LocalDate,
    val isEssential: Boolean = false
)