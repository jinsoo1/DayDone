package com.jsworld.android.daydone.presentation.ledger.model

import com.jsworld.android.daydone.domain.model.LedgerEntryKind
import java.time.LocalDate

data class LedgerEntryUiModel(
    val kind: LedgerEntryKind,
    val title: String,
    val amountText: String,  // 부호 포함 ("−45,000원" / "+300,000원")
    val tag: String?         // "준비금" / "저축" / "고정비" / "필수" (없으면 null)
)

data class LedgerDayUiModel(
    val date: LocalDate,
    val dayText: String,     // "1"
    val weekText: String,    // "화"
    /** 목록에서 달이 바뀌는 첫 날에만 채워진다 ("9월"). 기간이 두 달에 걸칠 때 구분용. */
    val monthLabel: String?,
    val isToday: Boolean,
    val spent: Long,
    val income: Long,
    val deducted: Long,
    val entries: List<LedgerEntryUiModel>
)

data class LedgerUiState(
    val isLoading: Boolean = true,
    val monthTitle: String = "",
    val periodText: String = "",
    /** true = 오래된 날짜부터(기간 첫날 → 마지막 날) */
    val ascending: Boolean = true,
    val monthlyBudget: Long = 0L,
    val incomeTotal: Long = 0L,
    val spentTotal: Long = 0L,
    val deductedTotal: Long = 0L,
    /** 월 예산 + 추가수익 − 저축·고정비 − 지출 (월 탭 예산 요약의 "남은 금액"과 같은 식) */
    val remaining: Long = 0L,
    /** 오늘이 이 기간 안이면 true — 라벨을 "남은 생활비"로 쓴다 */
    val isCurrentPeriod: Boolean = false,
    val days: List<LedgerDayUiModel> = emptyList()
)
