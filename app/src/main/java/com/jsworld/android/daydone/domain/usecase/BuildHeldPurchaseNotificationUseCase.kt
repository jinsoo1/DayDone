package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.domain.model.NotificationText
import com.jsworld.android.daydone.notification.NotificationTarget
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * 보류함 30일 도래 알림 — 오늘 딱 30일째가 되는 항목만.
 *
 * "딱 한 번 묻고 끝"이 30일 룰의 설계다. 지난 항목까지 매일 다시 알리지 않는다.
 */
class BuildHeldPurchaseNotificationUseCase @Inject constructor(
    private val observeHeldPurchasesUseCase: ObserveHeldPurchasesUseCase
) {
    suspend operator fun invoke(today: LocalDate): NotificationContent? {
        val due = observeHeldPurchasesUseCase().first()
            .filter { it.status == com.jsworld.android.daydone.domain.model.HeldPurchaseStatus.HELD }
            .filter { it.daysHeld(today) == HeldPurchase.HOLD_DAYS }

        if (due.isEmpty()) return null

        return NotificationContent(
            title = NotificationText.HeldPurchaseTitle(
                count = due.size,
                firstTitle = due.first().title
            ),
            body = NotificationText.HeldPurchaseBody(due.sumOf { it.amount }),
            target = NotificationTarget.HELD_PURCHASES
        )
    }
}
