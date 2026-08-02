package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.BudgetProfileRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

/** "가입 전 지출" 배너 처리 여부 관찰. */
class ObservePreJoinSpendHandledUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isPreJoinSpendHandledFlow
}

/** "가입 전 지출" 배너를 처리(입력 또는 건너뛰기)로 표시. */
class MarkPreJoinSpendHandledUseCase @Inject constructor(
    private val repository: BudgetProfileRepository
) {
    suspend operator fun invoke() = repository.setPreJoinSpendHandled()
}
