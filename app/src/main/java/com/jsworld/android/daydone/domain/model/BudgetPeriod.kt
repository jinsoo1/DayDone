package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

data class BudgetPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    fun contains(date: LocalDate): Boolean =
        !date.isBefore(startDate) && !date.isAfter(endDate)
}