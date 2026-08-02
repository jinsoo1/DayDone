package com.jsworld.android.daydone.domain.model

/**
 * 준비 항목의 현재 사이클 진행 상태(파생).
 * preparedAmount = 현재 사이클에 준비된 금액(= FUTURE_PREPARE 지출 합).
 */
data class FutureExpenseStatus(
    val item: FutureExpense,
    val preparedAmount: Long,
    val remainingAmount: Long,
    val isCompleted: Boolean // 1회 항목이 납부 완료된 상태
)
