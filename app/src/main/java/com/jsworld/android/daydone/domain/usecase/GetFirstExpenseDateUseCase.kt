package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ExpenseRepository
import jakarta.inject.Inject
import java.time.LocalDate

/**
 * 전체 기록 중 가장 오래된 지출 날짜 = "기록을 시작한 날".
 * 리포트 무지출 집계의 시작점으로 쓴다 — 그 전 날들은 기록이 없어
 * 가짜 무지출로 부풀기 때문. DataStore 상태(firstUseDate)와 달리
 * 데이터에서 파생되므로 복원·재설치에 흔들리지 않는다.
 */
class GetFirstExpenseDateUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): LocalDate? = repository.getEarliestExpenseDate()
}
