package com.jsworld.android.daydone.domain.model

import java.time.YearMonth

data class ScheduledDeduction(
    val id: Long,
    val title: String,
    val amount: Long,
    val type: ScheduledDeductionType,
    val withdrawalDay: Int,
    val startYearMonth: YearMonth,
    val endYearMonth: YearMonth?,
    val memo: String?
)