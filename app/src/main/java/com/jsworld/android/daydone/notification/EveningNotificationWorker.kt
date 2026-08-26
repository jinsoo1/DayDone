package com.jsworld.android.daydone.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/** 저녁 알림 — 기록 안내(오늘 지출 0건일 때만) + 내일 나갈 돈. 겹치면 한 건으로 합쳐진다. */
class EveningNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = notificationEntryPoint(applicationContext)
        val settings = entryPoint.observeNotificationSettingsUseCase()().first()

        if (!settings.anyEveningEnabled) return Result.success()

        runCatching {
            entryPoint.buildEveningNotificationUseCase()(LocalDate.now(), settings)?.let {
                entryPoint.notifier().show(
                    id = DayDoneNotifier.ID_EVENING,
                    title = it.title,
                    body = it.body,
                    target = it.target
                )
            }
        }

        entryPoint.scheduler().scheduleNext(
            work = NotificationScheduler.WORK_EVENING,
            hour = settings.eveningHour,
            workerIsMorning = false
        )

        return Result.success()
    }
}
