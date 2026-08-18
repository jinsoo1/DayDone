package com.jsworld.android.daydone.presentation.today.model

import com.jsworld.android.daydone.domain.model.PurchaseImpact

/**
 * 살까 말까 결과 — 계산 시점 스냅샷.
 * 시트가 열려 있는 동안 다른 지출이 들어와도 결과 숫자가 흔들리지 않게 값으로 들고 있는다.
 */
data class PurchaseEvaluationUiModel(
    val title: String,
    val price: Long,
    val currentDaily: Long,
    val afterDaily: Long,
    val budgetLeft: Long,
    val budgetLeftAfter: Long,
    val remainingDays: Int,
    val impact: PurchaseImpact
)
