package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.ScheduledDeduction
import jakarta.inject.Inject
import java.time.LocalDate

/**
 * 그 날짜에 출금되는 저축·고정비만 골라낸다.
 *
 * 출금일 clamp 규칙은 기간 UseCase 와 같다 — 달에 없는 날짜(29~31)는 **그 달 말일**로 본다.
 * 그래서 출금일 31일 항목은 2월엔 28일(윤년 29일)에 나간다.
 *
 * 항목이 이 기간에 살아 있는지(시작월·종료월)는
 * [GetScheduledDeductionsInPeriodUseCase] 가 이미 걸러주므로 여기서 다시 보지 않는다.
 */
class GetDeductionsDueOnUseCase @Inject constructor() {

    operator fun invoke(
        deductions: List<ScheduledDeduction>,
        date: LocalDate
    ): List<ScheduledDeduction> {
        return deductions.filter { deduction ->
            minOf(deduction.withdrawalDay, date.lengthOfMonth()) == date.dayOfMonth
        }
    }
}
