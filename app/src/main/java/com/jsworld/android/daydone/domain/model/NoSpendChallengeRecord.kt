package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

/** 끝난 무지출 챌린지 기록. */
data class NoSpendChallengeRecord(
    val startDate: LocalDate,
    val targetDays: Int,
    val successDays: Int,
    val mode: NoSpendMode,
    val capAmount: Long
) {
    val endDate: LocalDate get() = startDate.plusDays((targetDays - 1).toLong())
}
