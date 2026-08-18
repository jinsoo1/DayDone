package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.HeldPurchaseRepository
import jakarta.inject.Inject

/** 보류 항목/기록 삭제. 아낀 돈은 행의 합으로 파생되므로 PASSED 항목을 지우면 합계에서도 빠진다. */
class DeleteHeldPurchaseUseCase @Inject constructor(
    private val repository: HeldPurchaseRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.delete(id)
    }
}
