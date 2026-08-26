package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.presentation.util.toMoneyText
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth

/**
 * 내일 나가는 저축·고정비 안내 — 전날 저녁에 보낸다.
 *
 * ⚠️ **권장 금액은 이 알림 때문에 바뀌지 않는다.** 예정 차감은 출금일이 오기 전에 이미
 * 생활비에서 빼둔 돈이기 때문이다(§4). 그래서 목적은 예산 경고가 아니라 **출금 통장 잔액 확인**
 * 하나이고, 문구도 "이미 빼둔 돈"이라는 사실을 다시 확인해주는 방향으로 쓴다.
 *
 * 출금일은 기간 UseCase 와 **같은 clamp 규칙**(31일 → 그 달 말일)을 쓴다.
 */
class BuildUpcomingDeductionNoticeUseCase @Inject constructor(
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getCurrentBudgetPeriodUseCase: GetCurrentBudgetPeriodUseCase,
    private val observeScheduledDeductionsUseCase: ObserveScheduledDeductionsUseCase,
    private val getScheduledDeductionsInPeriodUseCase: GetScheduledDeductionsInPeriodUseCase,
    private val observeScheduledDeductionAmountsUseCase: ObserveScheduledDeductionAmountsUseCase,
    private val resolveScheduledDeductionAmountsUseCase: ResolveScheduledDeductionAmountsUseCase,
    private val getDeductionsDueOnUseCase: GetDeductionsDueOnUseCase
) {
    suspend operator fun invoke(today: LocalDate): NotificationContent? {
        val tomorrow = today.plusDays(1)

        val profile = observeBudgetProfileUseCase().first()

        // 내일이 속한 기간으로 본다 — 기간 경계를 넘는 날에도 어긋나지 않게.
        val period = getCurrentBudgetPeriodUseCase(
            today = tomorrow,
            budgetStartDay = profile.budgetStartDay
        )

        val deductions = resolveScheduledDeductionAmountsUseCase(
            deductions = getScheduledDeductionsInPeriodUseCase(
                deductions = observeScheduledDeductionsUseCase().first(),
                budgetPeriod = period
            ),
            overrides = observeScheduledDeductionAmountsUseCase().first(),
            anchorMonth = YearMonth.from(period.startDate)
        )

        val due = getDeductionsDueOnUseCase(deductions, tomorrow)
        if (due.isEmpty()) return null

        val total = due.sumOf { it.amount }

        val title = if (due.size == 1) {
            "내일 ${due.first().title} ${due.first().amount.toMoneyText()}이 나가요"
        } else {
            "내일 ${due.size}건 ${total.toMoneyText()}이 나가요"
        }

        val names = due.joinToString(" · ") { it.title }
        val body = if (due.size == 1) {
            "이미 생활비에선 빼둔 돈이라 통장만 확인해두면 돼요."
        } else {
            "$names. 이미 생활비에선 빼둔 돈이라 통장만 확인해두면 돼요."
        }

        return NotificationContent(title = title, body = body)
    }
}
