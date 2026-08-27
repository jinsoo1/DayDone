package com.jsworld.android.daydone.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

/** 저녁 알림 — 기록 안내(오늘 지출 0건일 때만) + 내일 나갈 돈. 겹치면 한 건으로 합쳐진다. */
class EveningNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = notificationEntryPoint(applicationContext)
        val settings = entryPoint.observeNotificationSettingsUseCase()().first()

        Log.i(TAG, "저녁 워커 실행 (예정 ${settings.eveningHour}시, 지금 ${LocalTime.now().hour}시)")

        if (!settings.anyEveningEnabled) {
            Log.i(TAG, "켜진 저녁 알림이 없어 건너뜀")
            return Result.success()
        }

        runCatching {
            val content = entryPoint.buildEveningNotificationUseCase()(LocalDate.now(), settings)
            if (content == null) {
                Log.i(TAG, "보낼 저녁 내용이 없음")
            } else {
                entryPoint.notifier().show(
                    id = DayDoneNotifier.ID_EVENING,
                    title = content.title,
                    body = content.body,
                    target = content.target
                )
            }
        }.onFailure { Log.w(TAG, "저녁 알림 생성 실패", it) }

        entryPoint.scheduler().scheduleNext(
            work = NotificationScheduler.WORK_EVENING,
            hour = settings.eveningHour,
            workerIsMorning = false
        )

        return Result.success()
    }

    private companion object {
        const val TAG = "DayDoneNoti"
    }
}
