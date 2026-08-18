package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.repository.HeldPurchaseRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * 보류함 항목 전체 (최신 보류일 순).
 * 30일 자동 전환은 저장하지 않으므로 화면에서 [HeldPurchase.isAutoPassed] 등으로 파생한다.
 */
class ObserveHeldPurchasesUseCase @Inject constructor(
    private val repository: HeldPurchaseRepository
) {
    operator fun invoke(): Flow<List<HeldPurchase>> = repository.observeAll()
}
