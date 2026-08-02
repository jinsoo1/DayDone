package com.jsworld.android.daydone.domain.model

import java.time.YearMonth

data class ScheduledDeductionAmount(
    val deductionId: Long,
    val anchorMonth: YearMonth,
    val amount: Long
)
