package com.jsworld.android.daydone.domain.model

/** 오늘 기준 남은 순수 생활비와 남은 일수 — 살까 말까 재계산 입력. */
data class PureBudgetSnapshot(
    val remainingPureBudget: Long,
    val remainingDays: Int
)
