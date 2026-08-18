package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.HeldPurchaseRepository
import jakarta.inject.Inject
import java.time.LocalDate

/** 살까 말까에서 "보류할게요" — 보류함에 넣는다. */
class HoldPurchaseUseCase @Inject constructor(
    private val repository: HeldPurchaseRepository
) {
    suspend operator fun invoke(
        title: String,
        amount: Long,
        heldAt: LocalDate
    ) {
        repository.hold(
            title = title,
            amount = amount,
            heldAt = heldAt
        )
    }
}
