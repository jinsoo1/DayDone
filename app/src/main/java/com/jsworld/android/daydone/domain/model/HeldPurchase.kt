package com.jsworld.android.daydone.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 보류 항목의 저장 상태. 30일 자동 전환은 저장하지 않고 조회 시 파생한다. */
enum class HeldPurchaseStatus {
    HELD,    // 보류 중
    PASSED,  // 안 삼 → 아낀 돈
    BOUGHT   // 결국 삼
}

/**
 * 소비 보류함 항목.
 * 30일 룰: 보류한 날로부터 30일이 지나면(= daysHeld ≥ 30) 자동으로 "아낀 돈"이 된다.
 * 저장 status 는 HELD 그대로 두고 [isAutoPassed] 로 파생한다 — 백그라운드 작업 불필요.
 */
data class HeldPurchase(
    val id: Long,
    val title: String,
    val amount: Long,
    val heldAt: LocalDate,
    val status: HeldPurchaseStatus,
    val resolvedAt: LocalDate?
) {
    /** 보류 n일째 (보류한 날 = 0일째). */
    fun daysHeld(today: LocalDate): Int =
        ChronoUnit.DAYS.between(heldAt, today).toInt().coerceAtLeast(0)

    /** 30일 창에서 남은 날. */
    fun daysLeft(today: LocalDate): Int =
        (HOLD_DAYS - daysHeld(today)).coerceAtLeast(0)

    /** 아직 30일 창 안에서 보류 중인가. */
    fun isHolding(today: LocalDate): Boolean =
        status == HeldPurchaseStatus.HELD && daysHeld(today) < HOLD_DAYS

    /** 30일이 지나 자동으로 아낀 돈이 된 상태 (저장은 HELD 그대로). */
    fun isAutoPassed(today: LocalDate): Boolean =
        status == HeldPurchaseStatus.HELD && daysHeld(today) >= HOLD_DAYS

    /** 아낀 돈으로 집계되는가 (직접 "안 살래요" 또는 30일 자동 전환). */
    fun isSaved(today: LocalDate): Boolean =
        status == HeldPurchaseStatus.PASSED || isAutoPassed(today)

    /** 보류한 날부터 상태 확정까지 걸린 날. 확정 전이면 null. */
    fun daysToResolve(): Int? = resolvedAt?.let {
        ChronoUnit.DAYS.between(heldAt, it).toInt().coerceAtLeast(0)
    }

    companion object {
        const val HOLD_DAYS = 30
    }
}
