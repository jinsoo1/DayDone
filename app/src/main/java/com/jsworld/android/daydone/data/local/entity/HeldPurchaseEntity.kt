package com.jsworld.android.daydone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 소비 보류함 항목 — 살까 말까에서 "보류할게요"를 고른 물건.
 * status 는 HELD / PASSED(안 삼 → 아낀 돈) / BOUGHT(결국 삼).
 * 30일 자동 전환은 저장하지 않고 조회 시 파생한다 (heldAt + 30 ≤ today).
 */
@Entity(tableName = "held_purchases")
data class HeldPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Long,
    val heldAt: String,        // "YYYY-MM-DD" 보류한 날
    val status: String,        // HELD / PASSED / BOUGHT
    val resolvedAt: String?,   // 상태 확정일 (HELD 면 null)
    val createdAt: Long = System.currentTimeMillis()
)
