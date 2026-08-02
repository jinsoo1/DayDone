package com.jsworld.android.daydone.presentation.today.model

import com.jsworld.android.daydone.domain.model.ScheduledDeductionType
import java.time.LocalDate

data class TodayScheduledDeductionUiModel(
    val id: Long,
    val title: String,
    val amount: Long,
    val type: ScheduledDeductionType,
    val withdrawalDate: LocalDate
)