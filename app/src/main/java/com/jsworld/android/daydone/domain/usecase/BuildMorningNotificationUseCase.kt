package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.notification.NotificationTarget
import com.jsworld.android.daydone.presentation.util.toMoneyText
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * 아침 알림 한 건을 만든다.
 *
 * 권장 금액·결산·큰 지출 안내가 같은 날 겹치면 **한 건으로 합친다**.
 * 알림 세 개가 연달아 오면 그것만으로 잔소리가 된다 (docs/v1.4-design.md §1).
 * 보낼 내용이 없으면 null.
 */
class BuildMorningNotificationUseCase @Inject constructor(
    private val observeDailyBudgetUseCase: ObserveDailyBudgetUseCase,
    private val observeBudgetProfileUseCase: ObserveBudgetProfileUseCase,
    private val getBigSpendMonthNoticeUseCase: GetBigSpendMonthNoticeUseCase
) {
    suspend operator fun invoke(
        today: LocalDate,
        settings: NotificationSettings
    ): NotificationContent? {
        val budget = observeDailyBudgetUseCase(today).first()
        val isPeriodFirstDay = budget.period.startDate == today

        val lines = mutableListOf<String>()

        // 기간 첫날: 지난 기간 결산
        val hasReport =
            settings.periodReportEnabled && isPeriodFirstDay && hasPreviousPeriod(today)
        if (hasReport) {
            lines += "지난 기간 결산이 나왔어요. 눌러서 확인해보세요."
        }

        // 기간 첫날: 큰 지출이 예상되는 달 안내
        var hasBigSpend = false
        if (settings.bigSpendMonthEnabled && isPeriodFirstDay) {
            getBigSpendMonthNoticeUseCase(YearMonth.from(today))?.let {
                lines += it
                hasBigSpend = true
            }
        }

        if (settings.morningEnabled) {
            lines += "이번 기간 ${budget.remainingDays}일 남았어요."
        }

        if (lines.isEmpty()) return null

        val title = if (settings.morningEnabled) {
            "오늘은 ${budget.todayRecommended.toMoneyText()}까지 괜찮아요"
        } else {
            "오늘의 데이던 소식이 있어요"
        }

        // 권장 금액이 없는 날엔 그날의 주인공 화면으로 보낸다.
        val target = when {
            settings.morningEnabled -> NotificationTarget.TODAY
            hasReport -> NotificationTarget.REPORT
            hasBigSpend -> NotificationTarget.VAULT
            else -> NotificationTarget.TODAY
        }

        return NotificationContent(
            title = title,
            body = lines.joinToString(" "),
            target = target
        )
    }

    /** 첫 사용 이전 기간의 결산은 안내하지 않는다 (가짜 "전액 지켜냄" 방지). */
    private suspend fun hasPreviousPeriod(today: LocalDate): Boolean {
        val firstUse = observeBudgetProfileUseCase().first().firstUseDate ?: return false
        return ChronoUnit.DAYS.between(firstUse, today) > 0 && firstUse.isBefore(today)
    }
}
