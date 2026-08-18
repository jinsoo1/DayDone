package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.HeldPurchaseStatus
import com.jsworld.android.daydone.domain.repository.HeldPurchaseRepository
import jakarta.inject.Inject
import java.time.LocalDate

/** 보류 항목 확정 — 샀거나(BOUGHT) 안 사기로 했거나(PASSED → 아낀 돈). */
class ResolveHeldPurchaseUseCase @Inject constructor(
    private val repository: HeldPurchaseRepository
) {
    suspend operator fun invoke(
        id: Long,
        status: HeldPurchaseStatus,
        resolvedAt: LocalDate
    ) {
        require(status != HeldPurchaseStatus.HELD) { "HELD 로는 확정할 수 없어요." }
        repository.resolve(
            id = id,
            status = status,
            resolvedAt = resolvedAt
        )
    }
}
