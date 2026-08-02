package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

data class ExtraIncome(
    val id: Long,
    val title: String,
    val amount: Long,
    val date: LocalDate,
    val memo: String?
)