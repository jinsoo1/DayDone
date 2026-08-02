package com.jsworld.android.daydone.domain.model

import java.time.YearMonth

/**
 * 준비 항목: 미리 돈을 모아둘 대상(세금·보험·기념일·기타).
 * 금고의 "준비됨"은 이 항목에 연결된 FUTURE_PREPARE 지출 합으로 파생한다.
 */
data class FutureExpense(
    val id: Long,
    val title: String,
    val category: FutureExpenseCategory,
    val totalAmount: Long,
    val targetYearMonth: YearMonth,        // 납부/이벤트 월
    val prepareStartYearMonth: YearMonth,  // 준비 시작 월
    val repeat: FutureExpenseRepeat,
    val memo: String?,
    val lastPaidYearMonth: YearMonth?      // 마지막 납부 완료 사이클 (null = 아직 없음)
)
