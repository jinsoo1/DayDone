package com.jsworld.android.daydone.domain.usecase

import com.jsworld.android.daydone.domain.model.HeldPurchase
import com.jsworld.android.daydone.domain.model.NotificationContent
import com.jsworld.android.daydone.notification.NotificationTarget
import com.jsworld.android.daydone.presentation.util.toMoneyText
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

        val title = if (due.size == 1) {
            "${due.first().title}, 30일이 지났어요"
        } else {
            "보류한 ${due.size}건이 30일을 넘겼어요"
        }

        val amount = due.sumOf { it.amount }.toMoneyText()

        return NotificationContent(
            title = title,
            body = "아직도 필요하면 그때 사요. 일단 ${amount}을 아낀 돈에 넣어뒀어요.",
            target = NotificationTarget.HELD_PURCHASES
        )
    }
}
