package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.repository.ScheduledDeductionRepository
import jakarta.inject.Inject
import java.time.YearMonth

/**
 * 고정비/저축 종료: [endYearMonth](보통 지금 보고 있는 기간의 anchorMonth)까지만 유지하고
 * 다음 기간부터 예정 차감에서 제외한다. 삭제와 달리 지난 기록은 그대로 남는다.
 */
class EndScheduledDeductionUseCase @Inject constructor(
    private val repository: ScheduledDeductionRepository
) {
    suspend operator fun invoke(
        id: Long,
        endYearMonth: YearMonth
    ) {
        repository.updateEndYearMonth(
            id = id,
            endYearMonth = endYearMonth
        )
    }
}
