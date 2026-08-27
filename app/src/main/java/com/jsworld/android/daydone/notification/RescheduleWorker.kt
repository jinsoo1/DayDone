package com.jsworld.android.daydone.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

/** 저장된 설정을 읽어 알림 예약을 다시 건다 (부팅 후·앱 시작 시). */
class RescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = notificationEntryPoint(applicationContext)
        val settings = entryPoint.observeNotificationSettingsUseCase()().first()

        if (settings.anyEnabled) {
            // 복구 경로 — 대기 중인 작업을 지우지 않는다 (NotificationScheduler.reschedule 주석 참고)
            entryPoint.scheduler().reschedule(settings, ExistingWorkPolicy.KEEP)
        }

        return Result.success()
    }
}
