package com.jsworld.android.daydone.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** 아침 알림 — 권장 금액·결산·큰 지출 안내를 한 건으로, 보류함 30일은 별도 한 건. */
class MorningNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = notificationEntryPoint(applicationContext)
        val settings = entryPoint.observeNotificationSettingsUseCase()().first()
        val today = LocalDate.now()

        if (!settings.anyMorningEnabled) return Result.success()

        runCatching {
            entryPoint.buildMorningNotificationUseCase()(today, settings)?.let {
                entryPoint.notifier().show(
                    id = DayDoneNotifier.ID_MORNING,
                    title = it.title,
                    body = it.body,
                    target = it.target
                )
            }

            if (settings.heldPurchaseEnabled) {
                entryPoint.buildHeldPurchaseNotificationUseCase()(today)?.let {
                    entryPoint.notifier().show(
                        id = DayDoneNotifier.ID_HELD,
                        title = it.title,
                        body = it.body,
                        target = it.target
                    )
                }
            }
        }

        // 실패해도 다음 날은 다시 건다 (한 번 실패로 알림이 영영 끊기지 않게)
        entryPoint.scheduler().scheduleNext(
            work = NotificationScheduler.WORK_MORNING,
            hour = settings.morningHour,
            workerIsMorning = true
        )

        return Result.success()
    }
}
