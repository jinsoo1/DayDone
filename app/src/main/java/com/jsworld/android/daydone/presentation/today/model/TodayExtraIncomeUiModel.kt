package com.jsworld.android.daydone.presentation.today.model

import java.time.LocalDate

data class TodayExtraIncomeUiModel(
    val id: Long,
    val title: String,
    val amount: Long,
    val date: LocalDate,
    val memo: String?
)
