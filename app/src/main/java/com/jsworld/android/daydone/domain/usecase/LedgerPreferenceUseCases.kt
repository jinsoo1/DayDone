package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/** 내역 화면 목록에 저축·고정비 줄을 보여줄지 — 보기 취향이라 기기에 남긴다. */
class ObserveLedgerDeductionsVisibleUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isLedgerDeductionsVisibleFlow
}

class SetLedgerDeductionsVisibleUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    suspend operator fun invoke(visible: Boolean) = repository.setLedgerDeductionsVisible(visible)
}

/** 내역 화면 정렬(기간 첫날부터 / 최근 날부터) — 같은 카드의 눈 아이콘과 같은 이유로 기억한다. */
class ObserveLedgerAscendingUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isLedgerAscendingFlow
}

class SetLedgerAscendingUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    suspend operator fun invoke(ascending: Boolean) = repository.setLedgerAscending(ascending)
}
