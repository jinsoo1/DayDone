package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity

/**
 * 고정비/저축 항목의 "월별 금액" 오버라이드.
 * 특정 anchorMonth 부터 적용될 금액을 저장하고, 조회 시 이월(carry-forward)한다.
 * 오버라이드가 없는 달은 ScheduledDeductionEntity.amount(최초 금액)로 폴백.
 */
@Entity(
    tableName = "scheduled_deduction_amounts",
    primaryKeys = ["deductionId", "anchorMonth"]
)
data class ScheduledDeductionAmountEntity(
    val deductionId: Long,
    val anchorMonth: String, // "YYYY-MM"
    val amount: Long
)
