package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

data class BudgetProfile(
    val monthlyIncome: Long,
    val payday: Int,
    val budgetStartDay: Int,
    val firstUseDate: LocalDate? = null // 온보딩 완료일 — 이전 기간 탐색/리포트 하한
)