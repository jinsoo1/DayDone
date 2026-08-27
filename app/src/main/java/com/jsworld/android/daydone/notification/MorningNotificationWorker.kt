package com.jsworld.android.daydone.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

/** 아침 알림 — 권장 금액·결산·큰 지출 안내를 한 건으로, 보류함 30일은 별도 한 건. */
class MorningNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = notificationEntryPoint(applicationContext)
        val settings = entryPoint.observeNotificationSettingsUseCase()().first()
        val today = LocalDate.now()

        Log.i(TAG, "아침 워커 실행 (예정 ${settings.morningHour}시, 지금 ${LocalTime.now().hour}시)")

        if (!settings.anyMorningEnabled) {
            Log.i(TAG, "켜진 아침 알림이 없어 건너뜀")
            return Result.success()
        }

        runCatching {
            val content = entryPoint.buildMorningNotificationUseCase()(today, settings)
            if (content == null) {
                Log.i(TAG, "보낼 아침 내용이 없음")
            } else {
                entryPoint.notifier().show(
                    id = DayDoneNotifier.ID_MORNING,
                    title = content.title,
                    body = content.body,
                    target = content.target
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
        }.onFailure { Log.w(TAG, "아침 알림 생성 실패", it) }

        // 실패해도 다음 날은 다시 건다 (한 번 실패로 알림이 영영 끊기지 않게)
        entryPoint.scheduler().scheduleNext(
            work = NotificationScheduler.WORK_MORNING,
            hour = settings.morningHour,
            workerIsMorning = true
        )

        return Result.success()
    }

    private companion object {
        const val TAG = "DayDoneNoti"
    }
}
