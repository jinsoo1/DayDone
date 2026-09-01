package com.jsworld.android.daydone.domain.model

import java.time.LocalDate

/**
 * 기간 내역(장부) 한 줄의 종류.
 * 표시용 문구·색은 presentation 에서 정한다 (domain 은 한글을 만들지 않는다).
 */
enum class LedgerEntryKind {
    EXTRA_INCOME,    // 추가 수익 (들어온 돈)
    EXPENSE,         // 일반 지출
    FUTURE_PREPARE,  // 준비금 (금고로 옮겨둔 지출)
    SAVING,          // 저축 출금
    FIXED            // 고정비 출금
}

data class LedgerEntry(
    val kind: LedgerEntryKind,
    val title: String,
    val amount: Long,
    val isEssential: Boolean = false,
    val expenseId: Long? = null,
    val extraIncomeId: Long? = null,
    val deductionId: Long? = null
)

/** 하루치 묶음. 기록이 하나도 없는 날은 만들지 않는다. */
data class LedgerDay(
    val date: LocalDate,
    val spent: Long,     // 일반 지출 + 준비금
    val income: Long,    // 추가 수익
    val deducted: Long,  // 저축 + 고정비 출금
    val entries: List<LedgerEntry>
)

/**
 * 한 예산 기간의 전체 내역. 모든 값은 계산 시점 파생 — 저장하지 않는다.
 * 합계는 정렬과 무관하게 기간 전체 기준.
 */
data class PeriodLedger(
    val days: List<LedgerDay>,
    val spentTotal: Long,
    val incomeTotal: Long,
    val deductedTotal: Long
)
